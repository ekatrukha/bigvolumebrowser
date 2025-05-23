package bvb.develop;

import com.jogamp.opengl.GL;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.imglib2.RealInterval;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Meshes;
import net.imglib2.mesh.impl.naive.NaiveFloatMesh;

import bvb.core.BigVolumeBrowser;
import bvb.shapes.MeshTexture;
import de.javagl.jgltf.model.AccessorData;
import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.BufferModel;
import de.javagl.jgltf.model.BufferViewModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.ImageModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.TextureModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import ij.ImageJ;

public class GLB_test
{
	public static void main( final String[] args )
	{
		//String sFilename = "src/test/resources/mesh/teddy.glb";
		String sFilename = "/home/eugene/Desktop/projects/BVB/GLB_read/5_20_2025.glb";
		
		GltfModelReader gltfModelReader = new GltfModelReader();
		GltfModel gltfModel = null;
		
		try
		{
			gltfModel = gltfModelReader.read(Paths.get(sFilename));
		}
		catch ( IOException exc )
		{
			exc.printStackTrace();
		}
		if(gltfModel != null)
		{       
			List<MeshModel> meshes = gltfModel.getMeshModels();
			MeshModel mesh = meshes.get( 0 );
			List< MeshPrimitiveModel > prmod = mesh.getMeshPrimitiveModels();
			MeshPrimitiveModel meshPrimitiveModel = prmod.get( 0 );
			final Mesh currMesh = new NaiveFloatMesh();
		
			final float [][] vert = readAttributeFloatArray(meshPrimitiveModel, "POSITION");
			final float [][] uvmap = readAttributeFloatArray(meshPrimitiveModel, "TEXCOORD_0");
			System.out.println( uvmap.length );
			for(int i = 0; i < vert.length; i++)
			{
				currMesh.vertices().addf(vert[i][0], vert[i][1], vert[i][2] );
				currMesh.vertices().setTexturef( i, uvmap[i][0], uvmap[i][1] ); 
			}
			System.out.println( vert.length );
			final int [] indices = readIndices(meshPrimitiveModel);
			for(int i = 0; i < indices.length; i++)
			{
				if((i+1)%3 == 0)
				{
					currMesh.triangles().add( indices[i-2], indices[i-1], indices[i] );
				}
			}
			System.out.println(indices.length);
			
			
			final MaterialModelV2 material = ( MaterialModelV2 ) meshPrimitiveModel.getMaterialModel();
			final TextureModel baseColorTexture = material.getBaseColorTexture();
			if (baseColorTexture == null) {
			    System.out.println("No base color texture found.");
			    return;
			}

			// 3. Get the image model
			final ImageModel imageModel = baseColorTexture.getImageModel();
			final BufferViewModel bufferViewModel = imageModel.getBufferViewModel();
			final BufferModel bufferModel = bufferViewModel.getBufferModel();
			final ByteBuffer byteBuffer = bufferModel.getBufferData();
	        final byte[] imageBytes = new byte[bufferViewModel.getByteLength()];
            byteBuffer.position(bufferViewModel.getByteOffset());
            byteBuffer.get(imageBytes);
			BufferedImage image = null;


			try
			{
				image = ImageIO.read(new ByteArrayInputStream(imageBytes));
			}
			catch ( IOException exc )
			{
				exc.printStackTrace();
			}
			
			new ImageJ();
		

			//start BVB
			BigVolumeBrowser bvbTest = new BigVolumeBrowser(); 		
			bvbTest.startBVB("");

//			MeshColor meshTeddy = new MeshColor(currMesh, bvbTest);
//			//meshTeddy.setPointsRender( 0.002f );
//			bvbTest.addShape( meshTeddy );
//			bvbTest.focusOnRealInterval( Meshes.boundingBox( currMesh ) );
			

			MeshTexture meshTeddyTextured = new MeshTexture(currMesh, image, bvbTest);
			bvbTest.addShape( meshTeddyTextured );
			RealInterval meshBBox =  Meshes.boundingBox( currMesh );
			bvbTest.focusOnRealInterval(  meshBBox  );
			
		}
		
	}
	
	public static float [][] readAttributeFloatArray(final MeshPrimitiveModel meshPrimitiveModel, String key)
	{
		
		float [][] out = null;
		Map<String, AccessorModel> attributes = meshPrimitiveModel.getAttributes();

		// POSITION is the key for vertex positions
		AccessorModel positionAccessor = attributes.get(key);
		
		if (positionAccessor != null) 
		{
			AccessorData accessorData = positionAccessor.getAccessorData();
			ByteBuffer byteBuffer = accessorData.createByteBuffer();
			FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
			int numComponents = accessorData.getNumComponentsPerElement();
			int count = accessorData.getNumElements(); // Number of vertices
			out = new float [count][numComponents];
			for (int i = 0; i < count; i++) 
			{
				for(int j = 0; j < numComponents; j++ )
				{
					out[i][j] = floatBuffer.get();
				}
			}
		}
		return out;
	}
	
	public static int [] readIndices(MeshPrimitiveModel meshPrimitiveModel) 
	{
	    
		final AccessorModel indexAccessor = meshPrimitiveModel.getIndices();
	    
		if (indexAccessor == null) 
	    {
	        System.out.println("No indices found (may be non-indexed geometry).");
	        return null;
	    }

		final AccessorData accessorData = indexAccessor.getAccessorData();
		final int indexCount = accessorData.getNumElements();
	    final int[] indices = new int[indexCount];
	    final ByteBuffer byteBuffer = accessorData.createByteBuffer();
	    
	    // Depending on component type, read the data appropriately
	    switch (indexAccessor.getComponentType()) {
	        case GL.GL_UNSIGNED_BYTE:
	        	final byte [] indicesB = new byte[indexCount];
	        	byteBuffer.get(indicesB);
	        	for(int i = 0; i< indexCount;i++)
	        	{
	        		 indices[i] = indicesB[i] & 0xFF;
	        	}
	        	return indices;
	        case GL.GL_UNSIGNED_SHORT:
	        	ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
	        	final short[] indicesS = new short[indexCount];
	        	shortBuffer.get(indicesS);
	        	for(int i = 0; i < indexCount;i++)
	        	{
	        		 indices[i] = Short.toUnsignedInt( indicesS[i] );
	        	}
	        	return indices;
	        case GL.GL_UNSIGNED_INT:	        	
	            final IntBuffer intBuffer = byteBuffer.asIntBuffer();
	            intBuffer.get(indices);
	            return indices;

	        default:
	            System.out.println("Unsupported component type for indices: " + indexAccessor.getComponentType());
	            return null;
	    }
	}
}