package trb.fps.editor;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.critterai.nmgen.NavmeshGenerator;
import org.critterai.nmgen.TriangleMesh;
import trb.fps.ai.NavigationMeshCreator;
import trb.fps.entity.Box;
import trb.fps.entity.SpawnPoint;
import trb.jsg.Shape;
import trb.jsg.TreeNode;
import trb.jsg.util.Mat4;
import trb.jsg.util.Vec3;

public class NavigationMeshEditorUser implements NavigationMeshCreator {

	Map<Box, TreeNode> boxNodeMap = new HashMap();

	public NavigationMeshEditorUser(Map<Box, TreeNode> boxNodeMap) {
		this.boxNodeMap = boxNodeMap;
	}

	public TriangleMesh create(NavmeshGenerator generator) {
		float[] vertices = {};
		int[] allIndices = {};
		Vec3 coord = new Vec3();
		for (Entry<Box, TreeNode> entry : boxNodeMap.entrySet()) {
			Box box = entry.getKey();
			if (box.getComponent(SpawnPoint.class) == null) {
				TreeNode treeNode = entry.getValue();
				for (Shape shape : treeNode.getAllShapesInTree()) {
					// TODO: use local2world of all shapes in tree
					Mat4 transform = treeNode.getTransform();
					float[] coords = getFloats(shape.getVertexData().coordinates);
					int[] indices = getInts(shape.getVertexData().indices);

					float[] newVertices = new float[vertices.length + coords.length];
					System.arraycopy(vertices, 0, newVertices, 0, vertices.length);
					for (int i = 0; i < coords.length; i += 3) {
						coord.set(coords[i], coords[i + 1], coords[i + 2]);
						transform.transformAsPoint(coord);
						newVertices[vertices.length + i + 0] = coord.x;
						newVertices[vertices.length + i + 1] = coord.y;
						newVertices[vertices.length + i + 2] = coord.z;
					}
					vertices = newVertices;

					int[] newIndices = new int[allIndices.length + indices.length];
					System.arraycopy(allIndices, 0, newIndices, 0, allIndices.length);
					for (int i = 0; i < indices.length; i++) {
						newIndices[allIndices.length + i] = indices[i] + allIndices.length;
					}
					allIndices = newIndices;
				}
			}
		}
		return generator.build(vertices, allIndices, null);
	}

	float[] getFloats(FloatBuffer buffer) {
		float[] floats = new float[buffer.rewind().limit()];
		buffer.get(floats).rewind();
		return floats;
	}

	int[] getInts(IntBuffer buffer) {
		int[] ints = new int[buffer.rewind().limit()];
		buffer.get(ints).rewind();
		return ints;
	}
}
