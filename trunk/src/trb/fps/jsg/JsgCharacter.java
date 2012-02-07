package trb.fps.jsg;

import trb.fps.jsg.shader.NormalMapping;
import trb.fps.net.PlayerPacket;
import trb.jsg.Shader;
import trb.jsg.Shape;
import trb.jsg.TreeNode;
import trb.jsg.VertexData;
import trb.jsg.util.Mat4;
import trb.jsg.util.Vec3;

public class JsgCharacter {

    public static JsgCharacter[] create(int cnt, TreeNode root) {
        JsgCharacter[] array = new JsgCharacter[cnt];
        for (int i=0; i<array.length; i++) {
            array[i] = new JsgCharacter();
            root.addChild(array[i].root);
        }
        return array;
    }

    TreeNode root;
    TreeNode leftArmNode;
    TreeNode rightArmNode;
    TreeNode leftFootNode;
    TreeNode rightFootNode;
    TreeNode leftEyeNode;
    TreeNode rightEyeNode;
    PlayerPacket prevPlayer = null;
    float distance = 0;

    public JsgCharacter() {
        Shader bodyShader = NormalMapping.createNoMapsShader(0.8f, 0.9f, 0.4f, 0.2f);
        Shader footShader = NormalMapping.createNoMapsShader(0.8f, 0.6f, 0.4f, 0.8f);
        Shader eyebrowShader = NormalMapping.createNoMapsShader(0.3f, 0.3f, 0.3f, 0.3f);
        Shader mouthShader = NormalMapping.createNoMapsShader(1f, 1f, 1f, 1f);

        VertexData body = JsgBox.createFromMinMax(-0.25f, 0.3f, -0.15f, 0.25f, 1.8f, 0.15f);
        Shape shape = new Shape(body);
        JsgDeferredRenderer.applyState(shape, bodyShader);
        root = new TreeNode(shape);

        VertexData armData = JsgBox.createFromMinMax(-0.05f, -0.1f, -0.05f, 0.05f, 0.7f, 0.05f);

        Shape leftArm = new Shape(armData);
        JsgDeferredRenderer.applyState(leftArm, bodyShader);
        leftArmNode = new TreeNode(leftArm);
        leftArmNode.setTransform(new Mat4().setTranslation_(new Vec3(-0.35f, 1.3f, 0f)).setEulerDeg(new Vec3(180, 0, 0)));
        root.addChild(leftArmNode);

        Shape rightArm = new Shape(armData);
        JsgDeferredRenderer.applyState(rightArm, bodyShader);
        rightArmNode = new TreeNode(rightArm);
        rightArmNode.setTransform(new Mat4().setTranslation_(new Vec3(0.35f, 1.3f, 0f)).setEulerDeg(new Vec3(180, 0, 0)));
        root.addChild(rightArmNode);

        VertexData footData = JsgBox.createFromMinMax(-0.1f, -0.25f, -0.2f, 0.1f, -0.1f, 0.1f);

        Shape leftFoot = new Shape(footData);
        JsgDeferredRenderer.applyState(leftFoot, footShader);
        leftFootNode = new TreeNode(leftFoot);
        leftFootNode.setTransform(new Mat4().setTranslation_(new Vec3(-0.25f, .25f, 0f)).setEulerDeg(new Vec3(0, 0, 0)));
        root.addChild(leftFootNode);

        Shape rightFoot = new Shape(footData);
        JsgDeferredRenderer.applyState(rightFoot, footShader);
        rightFootNode = new TreeNode(rightFoot);
        rightFootNode.setTransform(new Mat4().setTranslation_(new Vec3(0.25f, .25f, 0f)).setEulerDeg(new Vec3(0, 0, 0)));
        root.addChild(rightFootNode);

        VertexData eyeData = JsgBox.createFromPosSize(new Vec3(), new Vec3(0.15f, 0.15f, 0.15f));

        Shape leftEye = new Shape(eyeData);
        JsgDeferredRenderer.applyState(leftEye, mouthShader);
        leftEyeNode = new TreeNode(leftEye);
        leftEyeNode.setTransform(new Mat4().setTranslation_(new Vec3(-0.15f, 1.4f, -0.14f)).setEulerDeg(new Vec3(0, 0, 0)));
        root.addChild(leftEyeNode);

        Shape rightEye = new Shape(eyeData);
        JsgDeferredRenderer.applyState(rightEye, mouthShader);
        rightEyeNode = new TreeNode(rightEye);
        rightEyeNode.setTransform(new Mat4().setTranslation_(new Vec3(0.15f, 1.4f, -0.14f)).setEulerDeg(new Vec3(0, 0, 0)));
        root.addChild(rightEyeNode);

        VertexData pupilData = JsgBox.createFromPosSize(new Vec3(), new Vec3(0.05f, 0.05f, 0.15f));
        Shape leftPupil = new Shape(pupilData);
        JsgDeferredRenderer.applyState(leftPupil, eyebrowShader);
        TreeNode leftPupilNode = new TreeNode(leftPupil);
        leftPupilNode.setTransform(new Mat4().setTranslation_(new Vec3(-0.15f, 1.4f, -0.15f)));
        root.addChild(leftPupilNode);
        Shape rigthPupil = new Shape(pupilData);
        JsgDeferredRenderer.applyState(rigthPupil, eyebrowShader);
        TreeNode rigthPupilNode = new TreeNode(rigthPupil);
        rigthPupilNode.setTransform(new Mat4().setTranslation_(new Vec3(0.15f, 1.4f, -0.15f)));
        root.addChild(rigthPupilNode);


        VertexData browData = JsgBox.createFromPosSize(new Vec3(), new Vec3(0.175f, 0.05f, 0.15f));

        Shape leftEyebrow = new Shape(browData);
        JsgDeferredRenderer.applyState(leftEyebrow, eyebrowShader);
        TreeNode leftEyebrowNode = new TreeNode(leftEyebrow);
        leftEyebrowNode.setTransform(new Mat4().setTranslation_(new Vec3(-0.15f, 1.6f, -0.15f)).setEulerDeg(new Vec3(0, 0, -30)));
        root.addChild(leftEyebrowNode);

        Shape rigthEyebrow = new Shape(browData);
        JsgDeferredRenderer.applyState(rigthEyebrow, eyebrowShader);
        TreeNode rigthEyebrowNode = new TreeNode(rigthEyebrow);
        rigthEyebrowNode.setTransform(new Mat4().setTranslation_(new Vec3(0.15f, 1.6f, -0.15f)).setEulerDeg(new Vec3(0, 0, 30)));
        root.addChild(rigthEyebrowNode);

        VertexData noseData = JsgBox.createFromPosSize(new Vec3(), new Vec3(0.15f, 0.35f, 0.15f));
        Shader noseShader = NormalMapping.createNoMapsShader(1f, 0.3f, 0.3f, 0.7f);
        Shape nose = new Shape(noseData);
        JsgDeferredRenderer.applyState(nose, noseShader);
        TreeNode noseNode = new TreeNode(nose);
        noseNode.setTransform(new Mat4().setTranslation_(new Vec3(0f, 1.1f, -0.15f)).setEulerDeg(new Vec3(30, 0, 0)));
        root.addChild(noseNode);

        VertexData mouthData = JsgBox.createFromPosSize(new Vec3(), new Vec3(0.4f, 0.2f, 0.15f));
        Shape mouth = new Shape(mouthData);
        JsgDeferredRenderer.applyState(mouth, mouthShader);
        TreeNode mouthNode = new TreeNode(mouth);
        mouthNode.setTransform(new Mat4().setTranslation_(new Vec3(0f, 0.7f, -0.15f)));
        root.addChild(mouthNode);
    }

    public void setTransform(Mat4 transform) {
        root.setTransform(transform);
    }

    public void setVisible(boolean visible) {
        for (Shape shape : root.getAllShapesInTree()) {
            shape.setVisible(visible);
        }
    }

    public void update(PlayerPacket player) {
        if (prevPlayer != null) {
            Vec3 posDif = player.getPosition().sub_(prevPlayer.getPosition());
            distance += posDif.dot(player.getHeadingVector());
        }

        prevPlayer = new PlayerPacket(player);
        float armAngle = (float) Math.sin(distance * 4) * 60f;
        float footAngle = (float) Math.sin(distance * 4) * 80f;
        leftArmNode.getTransform().setEulerDeg(new Vec3(180-armAngle, 0, 0));
        rightArmNode.getTransform().setEulerDeg(new Vec3(180+armAngle, 0, 0));
        leftFootNode.getTransform().setEulerDeg(new Vec3(-footAngle, 0, 0));
        rightFootNode.getTransform().setEulerDeg(new Vec3(footAngle, 0, 0));
    }
}
