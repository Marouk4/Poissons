package fr.maxime;

public class Utils {

    /**
     * Fonction cosinus
     * @param angle Angle en degré
     * @return cosinus de l'angle
     */
    public static double cos(double angle){
        return Math.cos(angle*Math.PI/180);
    }

    /**
     * Fonction sinus
     * @param angle Angle en degré
     * @return sinus de l'angle
     */
    public static double sin(double angle){
        return Math.sin(angle*Math.PI/180);
    }

    public static double distance(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2));
    }

    public static double changeCoordX(double x, double y, double xSystem, double ySystem, double rotationSystem){
        return (x-xSystem) * Utils.cos(rotationSystem) + (y-ySystem) * Utils.sin(rotationSystem);
    }

    public static double changeCoordY(double x, double y, double xSystem, double ySystem, double rotationSystem){
        return -(y-ySystem) * Utils.cos(rotationSystem) + (x-xSystem) * Utils.sin(rotationSystem);
    }
}
