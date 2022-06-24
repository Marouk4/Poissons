package fr.maxime;

import java.util.ArrayList;
import java.util.List;

public class Fish {

    private double x, y, rotation;
    private final float red;
    private final float green;
    private final float blue;
    private final boolean target;

    public static final double SIZE = 20;
    public static final double SIGHT = 100;
    public static final double SIGHT_WALL = 100;

    private double xCenterOfMass;
    private double yCenterOfMass;
    private int weighting;
    private double offset;

    private final List<Fish> closeFish = new ArrayList<>();

    public Fish(double x, double y, double r, float red, float green, float blue, boolean target) {
        this.x = x;
        this.y = y;
        this.rotation = r;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.target = target;
    }

    public void update(List<Fish> fishList) {
        x += Simulation.speed * Utils.cos(rotation);
        y += Simulation.speed * Utils.sin(rotation);

        if (x > Simulation.WIDTH) {
            x = 0;
        } else if (x < 0) {
            x = Simulation.WIDTH;
        }

        if (y > Simulation.HEIGHT) {
            y = 0;
        } else if (y < 0) {
            y = Simulation.HEIGHT;
        }

        if (Simulation.separation || Simulation.alignment || Simulation.cohesion) {

            xCenterOfMass = 0;
            yCenterOfMass = 0;
            weighting = 0;
            offset = 0;

            for (Fish fish : fishList) {
                if (fish == this) continue;

                if (closeFish.contains(fish)) {
                    double xTransformed = Utils.changeCoordX(fish.x, fish.y, x, y, rotation);
                    double yTransformed = Utils.changeCoordY(fish.x, fish.y, x, y, rotation);

                    if (Utils.distance(x, y, fish.x, fish.y) > SIGHT || (Math.abs(yTransformed) < -1.5*xTransformed) || (!Simulation.separation && !Simulation.alignment && !Simulation.cohesion)) {
                        closeFish.remove(fish);
                    } else {
                        updateAlongOtherFish(fish);
                    }
                } else {
                    if (Utils.distance(x, y, fish.x, fish.y) < Fish.SIGHT) {
                        double xTransformed = Utils.changeCoordX(fish.x, fish.y, x, y, rotation);
                        double yTransformed = Utils.changeCoordY(fish.x, fish.y, x, y, rotation);

                        if ((Math.abs(yTransformed) > -1.5*xTransformed)) {
                            closeFish.add(fish);
                            updateAlongOtherFish(fish);
                        }
                    }
                }
            }

            if (Simulation.cohesion && !closeFish.isEmpty()) {
                double yTransformed = Utils.changeCoordY(xCenterOfMass / weighting, yCenterOfMass / weighting, x, y, rotation);

                offset += (yTransformed > 0 ? -1 : 1) * Simulation.cohesionRate;
            }

            rotation += offset;
        }

        if(Simulation.wall){
            if (x + SIGHT_WALL * Utils.cos(rotation) > Simulation.WIDTH - 20 || x + SIGHT_WALL * Utils.cos(rotation) < 20 || y + SIGHT_WALL * Utils.sin(rotation) > Simulation.HEIGHT - 20 || y + SIGHT_WALL * Utils.sin(rotation) < 20) {
                double i = x, j = y;
                int count = 0;
                while (i < Simulation.WIDTH - 20 && i > 20 && j < Simulation.HEIGHT - 20 && j > 20) {
                    i += Simulation.speed * Utils.cos(rotation);
                    j += Simulation.speed * Utils.sin(rotation);
                    count++;
                }

                double rotate = 2;
                boolean modif = true;
                while (x + SIGHT_WALL * Utils.cos(rotation + rotate) > Simulation.WIDTH - 20 || x + SIGHT_WALL * Utils.cos(rotation + rotate) < 20 || y + SIGHT_WALL * Utils.sin(rotation + rotate) > Simulation.HEIGHT - 20 || y + SIGHT_WALL * Utils.sin(rotation + rotate) < 20) {
                    if (modif) {
                        modif = false;
                        rotate *= -1;
                    } else {
                        modif = true;
                        rotate += 2 * Math.signum(rotate);
                    }
                }

                rotation += rotate / count;
            }
        }
    }

    private void updateAlongOtherFish(Fish fish) {

        if (Simulation.separation) {
            offset += (Utils.changeCoordY(fish.x, fish.y, x, y, rotation) > 0 ? 1 : -1) * Simulation.separationRate * (1 - Utils.distance(x, y, fish.x, fish.y) * 1 / SIGHT);
        }

        if (Simulation.alignment) {
            offset += (fish.rotation - (rotation + offset)) * Simulation.alignmentRate / 360;
        }

        if (Simulation.cohesion) {
            xCenterOfMass += fish.x;
            yCenterOfMass += fish.y;
            weighting++;
        }
    }

    public void render() {

        if (target && Simulation.target) {
            Renderer.renderTargetedFish(x, y, rotation, 1);
        } else {
            Renderer.renderFish(x, y, rotation, red, green, blue, 1);
        }

        if (target) {
            if (Simulation.showInfo && Simulation.separation) {
                for (Fish fish : closeFish) {
                    Renderer.renderLine(x, y, fish.x, fish.y, 1f, 0f, 0f, (float) (1 - Utils.distance(x, y, fish.x, fish.y) * 1 / SIGHT));
                }
            }
        }

        if (Simulation.showInfo) {
            if (Simulation.alignment) {
                Renderer.renderLine(x, y, x + 50 * Utils.cos(rotation), y + 50 * Utils.sin(rotation), 0, 0, 0.75f, 1);
            }

            if (Simulation.cohesion && target) {
                Renderer.renderCircle(0, Math.PI * 2, xCenterOfMass / weighting, yCenterOfMass / weighting, 0, 5, 1, 1, 1, true, false);
            }
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRotation() {
        return rotation;
    }

    public boolean isTarget() {
        return target;
    }

    public List<Fish> getFishClose() {
        return closeFish;
    }
}
