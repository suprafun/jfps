/*
 * HALDEN VR PLATFORM
 *
 * RADIATION MODULE
 *
 * $RCSfile: $
 *
 * Author :
 * Date   :
 * Version: $Revision: $ ($Date: $)
 *
 * (c) 2000-2011 Halden Virtual Reality Centre <http://www.ife.no/vr/>,
 * Institutt for energiteknikk. All rights reserved.
 *
 * This code is the property of Halden VR Centre <vr-info@hrp.no> and may
 * only be used in accordance with the terms of the license agreement
 * granted.
 */

package trb.fps.editor;

import static java.lang.Math.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import trb.fps.property.Property;
import trb.fps.property.PropertyOwner;
import trb.jsg.util.Mat4;
import trb.jsg.util.Vec3;

/**
 *
 * @author tomrbryn
 */
public class EditorNavigation extends PropertyOwner {

    public Mat4 viewTransform = new Mat4().translate(0, 0, -10);

    public void setViewTransform(Mat4 transform) {
        viewTransform.set(transform);
        viewTransform.invert();
    }

    public Mat4 getViewTransform() {
        Mat4 transform = new Mat4(viewTransform);
        transform.invert();
        return transform;
    }


    /** The distance from the view to the target */
    public final Property<Float> distance = add("Distance", 10f);

    /** Sets whether or not tool is enabled. */
    public final Property<Boolean> enabled = add("Enabled", false);

    public final Property<Mode> mode = add("Mode", Mode.EXAMINE);

//    public final Property<Tuple3f> target = addMethod("Target");
//
//    public InputController keyboardController;

    // the size of an arrow in screen pixels
    private static final double FLY_UNIT_IN_PIXELS = 200;

    public enum Mode {EXAMINE, FLY};

    /** Rotation in radians per pixel moved by the mouse */
    private double examineSpeedFactor = 0.001;

    private float flySpeed = 7;

    public EditorNavigation() {
        
    }

//    protected void setTarget(Tuple3f newTarget) {
//        Mat4 navT3D = SGUtil.getWorldTransform(getViewNode());
//        Vec3 navPos = navT3D.getTranslation();
//        Vec3 forward = new Vec3(newTarget).sub_(navPos);
//
//        // only change if not viewing up or down
//        if (new Vec3(forward).set(1, 0).lengthSquared() > 0.1) {
//            navT3D.lookAtDir(forward);
//            navT3D.invert();
//            distance.set(navPos.distance(newTarget));
//            getViewNode().setTransform(navT3D);
//            updateWidget();
//        }
//    }
//
//    @Override
//    public Vec3 getTarget() {
//        Mat4 viewT3D = SGUtil.getTransform(getViewNode());
//        viewT3D.translate(new Vec3(0, 0, -distance.get()));
//        return viewT3D.getTranslation();
//    }

    public boolean handleMouseEvent(int dx, int dy) {
        dy *= -1;
        // move wheel one unit equals draggin 20 pixels
        int pixelsMoved = -Mouse.getDWheel() * 20 / 120;
        if (pixelsMoved != 0) {
            if (isExamining()) {
                zoomExamine(pixelsMoved);
            } else {
                moveFly(0, 0, flySpeed * pixelsMoved / FLY_UNIT_IN_PIXELS);
            }
        }
        if (!isExamining()) {
            return true;
        }
        boolean leftDown = Mouse.isButtonDown(0);
        boolean middleDown = Mouse.isButtonDown(2);
        boolean rightDown = Mouse.isButtonDown(1);

        if (middleDown || (leftDown && rightDown)) {
            zoomExamine(dy);
        } else {
            if (leftDown) {
                double dAngleY = (dy * examineSpeedFactor) * (PI * 2.0);
                double dAngleX = (dx * examineSpeedFactor) * (PI * 2.0);
                rotateZoomExamine(dAngleX, dAngleY, 1);
            }

            if (rightDown) {
                DisplayMode m = Display.getDisplayMode();
                panExamine(-dx / (double) m.getWidth(), dy / (double) m.getHeight());
            }
        }

        return true;
    }

    private void panExamine(double dx, double dy) {
        Mat4 viewT3D = getViewTransform();
        Vec3 right = viewT3D.getRight();
        Vec3 up = viewT3D.getUp();

        right.scale(dx * distance.get());
        up.scale(dy * distance.get());

        Vec3 center = viewT3D.getTranslation();
        center.add(right);
        center.add(up);

        viewT3D.setTranslation(center);
        setViewTransform(viewT3D);
    }

    private void zoomExamine(int pixelsMoved) {
        // zoom 0.5 percent for each pixel moved by the mouse
        rotateZoomExamine(0, 0, Math.pow(1.005, pixelsMoved));
    }

    /**
     * Rotate and zoom.
     * @param roty rotation about the y axis
     * @param rotx rotation about the view x axis
     * @param zoomScale how much to move the camera forward or backwards
     */
    private boolean rotateZoomExamine(double roty, double rotxz, double zoomScale) {
        if (roty == 0.0 && rotxz == 0.0 && zoomScale == 1.0) {
            return false;
        }

        Mat4 viewT3D = getViewTransform();
        viewT3D.translate(new Vec3(0, 0, -distance.get()));
        rotate(viewT3D, roty, rotxz);
        final float epsilon = 0.00001f;
        distance.set((float) (max(epsilon, distance.get()) * zoomScale));
        viewT3D.translate(new Vec3(0, 0, distance.get()));
        setViewTransform(viewT3D);
        return true;
    }

    private void rotate(Mat4 viewT3D, double roty, double rotxz) {
        if (roty != 0 || rotxz != 0) {
            Vec3 euler = Mat4.getEuler(viewT3D);
            euler.y -= roty;
            // keep angles inside the interval -PI/2 to PI/2
            euler.x = (float) max(-PI / 2, min(PI / 2, euler.x - rotxz));
            viewT3D.setEuler(euler);
        }
    }

//    public void update(float time, InputState inputState) {
//    	if (!isExamining() && inputState.isAnyMouseButtonsDown()) {
//            if (inputState.isMouseButtonDown(2)) {
//                double dx = inputState.getMouseFreezeDragDelta(2).x / FLY_UNIT_IN_PIXELS;
//                double dy = inputState.getMouseFreezeDragDelta(2).y / FLY_UNIT_IN_PIXELS;
//                rotateFly(dx * time, dy * time);
//            }
//            if (inputState.isMouseButtonDown(1)) {
//                double dx = inputState.getMouseFreezeDragDelta(1).x / FLY_UNIT_IN_PIXELS;
//                double dy = inputState.getMouseFreezeDragDelta(1).y / FLY_UNIT_IN_PIXELS;
//                rotateFly(dx * time, 0);
//                moveFly(0, 0, dy * flySpeed * time);
//            }
//            if (inputState.isMouseButtonDown(3)) {
//                double dx = inputState.getMouseFreezeDragDelta(3).x / FLY_UNIT_IN_PIXELS;
//                double dy = inputState.getMouseFreezeDragDelta(3).y / FLY_UNIT_IN_PIXELS;
//                moveFly(dx * 2 * time, -dy * 2 * time, 0);
//            }
//        }
//    }

//    private boolean updateKeyboardNavigation(float time) {
//        if (keyboardController == null) {
//            return false;
//        }
//
//        boolean changed = false;
//        final double pixelsPerSecond = 100.0;
//        final double degreesPerSecond = 45.0 / 360.0;
//        final double rotateAmount = -degreesPerSecond * time * 2.0 * PI;
//        if (isExamining()) {
//            double dAngleY = rotateAmount * keyboardController.getVerticalRotation();
//            double dAngleX = rotateAmount * keyboardController.getHorisontalRotation();
//            double zoom = Math.pow(1.005, pixelsPerSecond * time * -keyboardController.getZoom());
//            changed |= rotateZoomExamine(dAngleX, dAngleY, zoom);
//        } else {
//            double dx = rotateAmount * keyboardController.getHorisontalRotation();
//            double dy = -time * keyboardController.getVerticalRotation();
//            double dz = -time * keyboardController.getZoom();
//            changed |= rotateFly(dx, dz);
//            changed |= moveFly(0, 0, dy * flySpeed);
//        }
//        double dx = -keyboardController.getHorisontalMovement();
//        double dy = -keyboardController.getVerticalMovement();
//        changed |= moveFly(dx * 2 * time, -dy * 2 * time, 0);
//        return changed;
//    }

    private boolean isExamining() {
    	return mode.get() == Mode.EXAMINE;
    }

    private boolean rotateFly(double roty, double rotxz) {
        if (roty == 0.0 && rotxz == 0.0) {
            return false;
        }
        Mat4 viewT3D = getViewTransform();
        Vec3 euler = Mat4.getEuler(viewT3D);
        euler.y -= roty;
        // keep angles safely inside the interval -PI/2 to PI/2
        euler.x = (float) max(-PI / 2, min(PI / 2, euler.x - rotxz));
        viewT3D.setEuler(euler);
        setViewTransform(viewT3D);
        return true;
    }

    private boolean moveFly(double dx, double dy, double dz) {
        if (dx == 0.0 && dy == 0.0 && dz == 0.0) {
            return false;
        }
        Mat4 viewT3D = getViewTransform();
        viewT3D.translate(new Vec3(dx, dy, dz));
        setViewTransform(viewT3D);
        return true;
    }
}
