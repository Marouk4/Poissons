package fr.maxime;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {


    public static void renderFish(double x, double y, double rotation, float red, float green, float blue, double scale){
        glPushMatrix();

        glTranslated(x, y, 0);
        glRotated(rotation, 0, 0, 1);
        glScaled(scale,scale,0);

        glBegin(GL_TRIANGLES);

        glColor3f(red, green, blue);

        double angle = Math.PI / 10;
        glVertex2d(0, 0);
        glVertex2d(-Fish.SIZE*Math.cos(angle), Fish.SIZE*Math.sin(angle));
        glVertex2d(-Fish.SIZE*Math.cos(angle), -Fish.SIZE*Math.sin(angle));

        glEnd();

        glPopMatrix();
    }

    public static void renderTargetedFish(double x, double y, double rotation, double scale){
        renderFish(x,y,rotation,1,0,0,scale);
    }

    public static void renderCircle(double start, double end, double xOffset, double yOffset, double rotationOffset, double length, float red, float green, float blue, boolean filled, boolean specialFilled){
        renderCircle(start,end,xOffset,yOffset,rotationOffset,length,length,red,green,blue,filled,specialFilled);
    }

    public static void renderCircle(double start, double end, double xOffset, double yOffset, double rotationOffset, double lengthX, double lengthY, float red, float green, float blue, boolean filled, boolean specialFilled){
        glPushMatrix();

        glTranslated(xOffset,yOffset,0);
        glRotated(rotationOffset,0,0,1);

        if(filled){
            glBegin(GL_TRIANGLE_FAN);
        } else{
            glBegin(GL_LINE_LOOP);
        }

        glColor3f(red,green,blue);

        for (double i = start; i <= end; i += 0.001) {
            glVertex2d(lengthX * Math.cos(i), lengthY * Math.sin(i));
            if(specialFilled){
                glVertex2d(0,0);
            }
        }

        glEnd();

        glPopMatrix();
    }

    public static void renderLine(double xStart, double yStart, double xEnd, double yEnd, float red, float green, float blue, float alpha){
        glBegin(GL_LINE_STRIP);

        glColor4f(red,green,blue,alpha);

        glVertex2d(xStart,yStart);
        glVertex2d(xEnd,yEnd);

        glEnd();
    }

    public static void renderQuad(double xStart, double yStart, double length, float red, float green, float blue){
        glBegin(GL_QUADS);

        glColor3f(red,green,blue);

        glVertex2d(xStart,yStart);
        glVertex2d(xStart,yStart+length);
        glVertex2d(xStart+length,yStart+length);
        glVertex2d(xStart+length,yStart);

        glEnd();
    }
}
