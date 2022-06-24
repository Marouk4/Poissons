package fr.maxime;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;
import java.util.ArrayList;
import java.util.List;

import static fr.maxime.IOUtil.ioResourceToByteBuffer;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.stb.STBTruetype.stbtt_PackEnd;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Simulation {

    // The window handle
    private long window;

    // Taille de la fenêtre
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    private final List<Fish> fishList = new ArrayList<>();
    private Fish targetFish = null;

    // Souris
    double xMousePos;
    double yMousePos;

    // Propriétés
    public static boolean hud = false;
    public static boolean separation = false;
    public static boolean alignment = false;
    public static boolean cohesion = false;
    public static boolean showInfo = false;
    public static boolean radar = false;
    public static boolean properties = false;
    public static boolean density = false;
    public static boolean target = false;
    public static double speed = 2.5;
    public static boolean wall = false;
    public static boolean pause = false;

    public static double separationRate = 2;
    public static double alignmentRate = 4;
    public static double cohesionRate = 4;
    public static int densityPrecision = 50;
    public static int propertiesCount = 10;

    private int selection = 0;

    // Font
    private int font = 5;
    private boolean supportsSRGB;
    private int font_tex;
    private STBTTPackedchar.Buffer chardata;
    private static final int BITMAP_W = 512;
    private static final int BITMAP_H = 512;
    private static final float[] scale = {
            24.0f,
            14.0f
    };
    private final STBTTAlignedQuad q = STBTTAlignedQuad.malloc();
    private final FloatBuffer xb = memAllocFloat(1);
    private final FloatBuffer yb = memAllocFloat(1);

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        load_fonts();
        loop();

        GL.setCapabilities(null);

        chardata.free();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();

        memFree(yb);
        memFree(xb);

        q.free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        // Utiliser window = glfwCreateWindow(WIDTH, HEIGHT, "Poissons", glfwGetPrimaryMonitor(), NULL); pour le plein écran
        window = glfwCreateWindow(WIDTH, HEIGHT, "Poissons", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop

            if (action == GLFW_RELEASE) {
                switch (key) {
                    case GLFW_KEY_BACKSPACE -> pause = !pause;
                    case GLFW_KEY_SPACE -> {
                        if(separation && alignment && cohesion){
                            separation = false;
                            alignment = false;
                            cohesion = false;
                        } else {
                            separation = true;
                            alignment = true;
                            cohesion = true;
                        }
                    }
                    case GLFW_KEY_H -> hud = !hud;
                    case GLFW_KEY_S -> separation = !separation;
                    case GLFW_KEY_A -> alignment = !alignment;
                    case GLFW_KEY_C -> cohesion = !cohesion;
                    case GLFW_KEY_T -> target = !target;
                    case GLFW_KEY_I -> showInfo = !showInfo;
                    case GLFW_KEY_R -> radar = !radar;
                    case GLFW_KEY_D -> density = !density;
                    case GLFW_KEY_P -> properties = !properties;
                    case GLFW_KEY_W -> wall = !wall;
                    case GLFW_KEY_M -> {
                        if(glfwGetInputMode(window,GLFW_CURSOR) == GLFW_CURSOR_NORMAL){
                            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
                        } else {
                            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                        }
                    }
                    case GLFW_KEY_ENTER -> {
                        switch (selection){
                            case 0 -> separation = !separation;
                            case 1 -> alignment = !alignment;
                            case 2 -> cohesion = !cohesion;
                            case 3 -> density = !density;
                            case 5 -> showInfo = !showInfo;
                            case 4 -> radar = !radar;
                            case 6 -> target = !target;
                            case 7 -> fishList.add(new Fish(Math.random() * WIDTH, Math.random() * HEIGHT, Math.random() * 360, (float) (Math.random() % 0.2), (float) (Math.random() % 0.2), 1, false));
                            case 8 -> speed*=1.5d;
                            case 9 -> wall = !wall;
                        }
                    }
                    case GLFW_KEY_UP -> {
                        selection--;
                        if(selection < 0) selection = propertiesCount-1;
                    }
                    case GLFW_KEY_DOWN -> {
                        selection++;
                        selection%=propertiesCount;
                    }
                    case GLFW_KEY_KP_ADD -> {
                        switch (selection){
                            case 0 -> separationRate++;
                            case 1 -> alignmentRate++;
                            case 2 -> cohesionRate++;
                            case 3 -> densityPrecision++;
                            case 7 -> fishList.add(new Fish(Math.random() * WIDTH, Math.random() * HEIGHT, Math.random() * 360, (float) (Math.random() % 0.2), (float) (Math.random() % 0.2), 1, false));
                            case 8 -> speed*=1.5d;
                        }
                    }
                    case GLFW_KEY_KP_SUBTRACT -> {
                        switch (selection){
                            case 0 -> separationRate--;
                            case 1 -> alignmentRate--;
                            case 2 -> cohesionRate--;
                            case 3 -> densityPrecision--;
                            case 7 -> fishList.stream().filter(f -> !f.isTarget()).findFirst().ifPresent(fishList::remove);
                            case 8 -> speed/=1.5d;
                        }
                    }
                    case GLFW_KEY_KP_0 -> {
                        switch (selection){
                            case 0 -> separationRate=2;
                            case 1 -> alignmentRate=4;
                            case 2 -> cohesionRate=4;
                            case 3 -> densityPrecision=50;
                            case 8 -> speed = 2.5d;
                        }
                    }
                }
            }

        });

        glfwSetMouseButtonCallback(window, (window,button,action,mods) -> {
            if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS){
                for(int i = 0; i < 10; i++){
                    fishList.add(new Fish(xMousePos, yMousePos, Math.random() * 360, (float) (Math.random() % 0.2), (float) (Math.random() % 0.2), 1, false));
                }
            }
        });

        glfwSetCursorPosCallback(window, (window,x,y) -> {
            xMousePos = x;
            yMousePos = y;
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // Detect sRGB support
        GLCapabilities caps = GL.getCapabilities();
        supportsSRGB = caps.OpenGL30 || caps.GL_ARB_framebuffer_sRGB || caps.GL_EXT_framebuffer_sRGB;
    }

    private void load_fonts() {
        font_tex = glGenTextures();
        chardata = STBTTPackedchar.malloc(6 * 128);

        try (STBTTPackContext pc = STBTTPackContext.malloc()) {
            ByteBuffer ttf = ioResourceToByteBuffer("FiraSans-Thin.ttf", 512 * 1024);

            ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);

            stbtt_PackBegin(pc, bitmap, BITMAP_W, BITMAP_H, 0, 1, NULL);
            for (int i = 0; i < 2; i++) {
                int p = (i * 3 + 0) * 128 + 32;
                chardata.limit(p + 95);
                chardata.position(p);
                stbtt_PackSetOversampling(pc, 1, 1);
                stbtt_PackFontRange(pc, ttf, 0, scale[i], 32, chardata);

                p = (i * 3 + 1) * 128 + 32;
                chardata.limit(p + 95);
                chardata.position(p);
                stbtt_PackSetOversampling(pc, 2, 2);
                stbtt_PackFontRange(pc, ttf, 0, scale[i], 32, chardata);

                p = (i * 3 + 2) * 128 + 32;
                chardata.limit(p + 95);
                chardata.position(p);
                stbtt_PackSetOversampling(pc, 3, 1);
                stbtt_PackFontRange(pc, ttf, 0, scale[i], 32, chardata);
            }
            chardata.clear();
            stbtt_PackEnd(pc);

            glBindTexture(GL_TEXTURE_2D, font_tex);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, BITMAP_W, BITMAP_H, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        targetFish = new Fish(Math.random() * WIDTH, Math.random() * HEIGHT, Math.random() * 360, (float) (Math.random() % 0.2), (float) (Math.random() % 0.2), 1, true);
        fishList.add(targetFish);

        for (int i = 0; i < 200; i++) {
            fishList.add(new Fish(Math.random() * WIDTH, Math.random() * HEIGHT, Math.random() * 360, (float) (Math.random() % 0.2), (float) (Math.random() % 0.2), 1, false));
        }

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            update();
            render();

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void update() {
        if (pause) return;

        for (Fish fish : fishList) {
            fish.update(fishList);
        }
    }

    private void render() {
        // Set the clear color
        if(target){
            glClearColor(0.2f, 0.2f, 0.2f, 0.0f);
        } else {
            glClearColor(0.22f, 0.22f, 0.22f, 0.0f);
        }

        glDisable(GL_CULL_FACE);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glDisable(GL_DEPTH_TEST);
        glViewport(0, 0, WIDTH, HEIGHT);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, WIDTH, HEIGHT, 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        if(density){
            int dimX = (WIDTH/densityPrecision) +1;
            int dimY = (HEIGHT/densityPrecision) +1;

            int[][] tab = new int[dimX][dimY];

            for(Fish fish : fishList){
                tab[(int)(fish.getX()/densityPrecision)][(int) (fish.getY()/densityPrecision)]++;
            }

            for(int i = 0; i < dimX; i++){
                for(int j = 0; j < dimY; j++){
                    Renderer.renderQuad(i*densityPrecision,j*densityPrecision,densityPrecision,0,0, (tab[i][j] == 0 ? 0.75f : 0.5f /tab[i][j]));
                }
            }
        } else {
            if(target) Renderer.renderCircle(-Math.PI * 2 / 3, Math.PI * 2 / 3, targetFish.getX(), targetFish.getY(), targetFish.getRotation(), Fish.SIGHT, 0.22f, 0.22f, 0.22f, false, true);

            for (Fish fish : fishList) {
                if (fish != targetFish) {
                    fish.render();
                }
            }
            targetFish.render();
        }

        if(hud) {
            if(radar) renderRadar(targetFish);

            if (supportsSRGB) glEnable(GL30.GL_FRAMEBUFFER_SRGB);

            glColor3f(1.0f, 1.0f, 1.0f);

            int offset = HEIGHT;

            if(properties){
                int i = propertiesCount-1;

                print(10, (offset = offset-20), font, (selection == i-- ? "-> " : "") + "Wall: " + wall);
                print(10, (offset = offset-20), font, (selection == i-- ? "-> " : "") + "Speed: " + speed);
                print(10, (offset = offset-20), font, (selection == i-- ? "-> " : "") + "Fishes: " + fishList.size());
                print(10, (offset = offset-20), font, (selection == i-- ? "-> " : "") + "Target: " + (target ? "Yes" : "No"));
                print(10, (offset = offset-20), font, (selection == i-- ? "-> " : "") + "Radar: " + (radar ? "Yes" : "No"));
                print(10, (offset = offset-20), font, (selection == i-- ? "-> " : "") + "Informations: " + (showInfo ? "Yes" : "No"));
                print(10, (offset = offset-20), font, (selection == i-- ? "-> " : "") + "Density: " + (density ? "Yes" : "No") + ", " + densityPrecision);
                print(10, (offset = offset-20), font, (selection == i-- ? "-> " : "") + "Cohesion: " + (cohesion ? "Yes" : "No") + ", " + cohesionRate);
                print(10, (offset = offset-20), font, (selection == i-- ? "-> " : "") + "Alignment: " + (alignment ? "Yes" : "No") + ", " + alignmentRate);
                print(10, (offset = offset-20), font, (selection == i ? "-> " : "") + "Separation: " + (separation ? "Yes" : "No") + ", " + separationRate);
                print(10, offset-20, (font + 3) % 6, "Properties");
            } else {
                print(10, offset-20, (font + 3) % 6, "Properties (P)");
            }

            if(!radar) print(10, 30, (font + 3) % 6, "Radar (R)");

            if (supportsSRGB) glDisable(GL30.GL_FRAMEBUFFER_SRGB);
        }
    }

    private void renderRadar(Fish fish) {
        int size = Math.min(WIDTH, HEIGHT) / 5;

        Renderer.renderCircle(0, Math.PI * 2, size / 2d, size / 2d, 0, Fish.SIGHT * 100 / size, 0.1f, 0.2f, 0.1f, true, false);
        Renderer.renderCircle(-Math.PI * 2 / 3, Math.PI * 2 / 3, size / 2d, size / 2d, 0, Fish.SIGHT * 100 / size, 0.1f, 0.3f, 0.1f, false, true);
        Renderer.renderCircle(0, Math.PI * 2, size / 2d, size / 2d, 0, Fish.SIGHT * 100 / size, 0.1f, 0.5f, 0.1f, false, false);

        for (Fish f : fish.getFishClose()) {
            double xTransformed = size/2d + Utils.changeCoordX(f.getX(),f.getY(),fish.getX(),fish.getY(),fish.getRotation()) * 100 / size;
            double yTransformed = size/2d - Utils.changeCoordY(f.getX(),f.getY(),fish.getX(),fish.getY(),fish.getRotation()) * 100 / size;
            Renderer.renderFish(xTransformed, yTransformed,f.getRotation() - fish.getRotation(), 0.1f, 0.6f, 0.1f, Fish.SIGHT / size);
        }
        Renderer.renderTargetedFish((double) (size) / 2, (double) (size) / 2, 0, Fish.SIGHT / size);
    }

    private void print(float x, float y, int font, String text) {
        xb.put(0, x);
        yb.put(0, y);

        chardata.position(font * 128);

        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, font_tex);

        glBegin(GL_QUADS);
        for (int i = 0; i < text.length(); i++) {
            stbtt_GetPackedQuad(chardata, BITMAP_W, BITMAP_H, text.charAt(i), xb, yb, q, false);
            drawBoxTC(
                    q.x0(), q.y0(), q.x1(), q.y1(),
                    q.s0(), q.t0(), q.s1(), q.t1()
            );
        }
        glEnd();
    }

    private static void drawBoxTC(float x0, float y0, float x1, float y1, float s0, float t0, float s1, float t1) {
        glTexCoord2f(s0, t0);
        glVertex2f(x0, y0);
        glTexCoord2f(s1, t0);
        glVertex2f(x1, y0);
        glTexCoord2f(s1, t1);
        glVertex2f(x1, y1);
        glTexCoord2f(s0, t1);
        glVertex2f(x0, y1);
    }

    public static void main(String[] args) {
        new Simulation().run();
    }

}
