/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bvb.scene;


import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import bvb.core.BVVSettings;

import net.imglib2.RealPoint;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector4f;

import bvvpg.core.backend.jogl.JoglGpuContext;
import bvvpg.core.shadergen.DefaultShader;
import bvvpg.core.shadergen.Shader;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;

import static com.jogamp.opengl.GL.GL_FLOAT;

/** example class that draws point of specific shape with different filling **/

public class VisSpotsSame
{
	public static final int RENDER_FILLED = 0, RENDER_OUTLINE = 1, RENDER_GAUSS = 2; 

	public static final int SHAPE_ROUND = 0, SHAPE_SQUARE = 1; 
	
	private Shader prog;

	private int vao;
	
	private Vector4f l_color;
	
	private float fSpotSize;
	
	private int renderType = 0;
	
	private int spotShape = 0;
	
	float vertices[]; 
	
	private int nSpotsN;
	
	private boolean initialized;
	
	volatile boolean bLocked = false;
	
	public VisSpotsSame()
	{
		initShader();
	}
	
	void initShader()
	{
		final Segment pointVp = new SegmentTemplate( VisSpotsSame.class, "/scene/scaled_point.vp" ).instantiate();
		final Segment pointFp = new SegmentTemplate( VisSpotsSame.class, "/scene/scaled_point.fp" ).instantiate();		
		prog = new DefaultShader( pointVp.getCode(), pointFp.getCode() );
	}
	
	/** constructor with multiple vertices **/
	public VisSpotsSame(final ArrayList< RealPoint > points, final float fSpotSize_, final Color color_in, final int nShape_, final int nRenderType_)
	{
		this();
		
		int i,j;
		
		fSpotSize = fSpotSize_;
		
		l_color = new Vector4f(color_in.getComponents(null));
		
		nSpotsN = points.size();
		
		renderType = nRenderType_;
		
		spotShape = nShape_;
		
		vertices = new float [nSpotsN*3];//assume 3D

		for (i=0;i<nSpotsN; i++)
		{
			for (j=0;j<3; j++)
			{				
				vertices[i*3+j]=points.get(i).getFloatPosition(j);
			}			
		}

	}
	
	public void setVertices( ArrayList< RealPoint > points)
	{
		int i,j;	
		
		nSpotsN = points.size();
		
		if(nSpotsN == 1)
			vertices = new float [nSpotsN*3]; //assume 3D
		else
			vertices = new float [(nSpotsN+1)*3]; //assume 3D

		
		for (i=0;i<nSpotsN; i++)
		{
			for (j=0;j<3; j++)
			{
				vertices[i*3+j] = points.get(i).getFloatPosition(j);
			}			
		}
		
		if(nSpotsN>1)
		{
			i = nSpotsN-1;
			for (j=0;j<3; j++)
			{
				vertices[(i+1)*3+j] = points.get(i).getFloatPosition(j);
			}			
		}
		
		initialized = false;
	}
	
	public void setColor(Color pointColor) 
	{
		
		l_color = new Vector4f(pointColor.getComponents(null));
		
	}
	
	public void setSize(float fSpotSize_)
	{
		fSpotSize = fSpotSize_;
	}
	
	/** 0 - filled, 1 - outline **/
	public void setRenderType(int nRenderType_)
	{
		renderType = nRenderType_;
		
	}
	/** 0 - round, 1 - square **/
	public void setShape(int nShape_)
	{
		spotShape = nShape_;
		
	}

	private void init( GL3 gl )
	{
		
		while (bLocked)
		{
			try
			{
				Thread.sleep( 10 );
			}
			catch ( InterruptedException exc )
			{
				exc.printStackTrace();
			}
		}
		
		bLocked = true;		

		// ..:: VERTEX BUFFER ::..

		final int[] tmp = new int[ 2 ];
		gl.glGenBuffers( 1, tmp, 0 );
		final int vbo = tmp[ 0 ];
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, vbo );
		gl.glBufferData( GL.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, FloatBuffer.wrap( vertices ), GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );


		// ..:: VERTEX ARRAY OBJECT ::..

		gl.glGenVertexArrays( 1, tmp, 0 );
		vao = tmp[ 0 ];
		gl.glBindVertexArray( vao );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, vbo );
		gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 0 );
		gl.glBindVertexArray( 0 );
		
		initialized = true;
		bLocked  = false;

	}
	
	public void reload()
	{
		initShader();
		initialized = false;
	}

	public void draw(final GL3 gl, final Matrix4fc pvm, final int [] screen_size )
	{
		
		if (fSpotSize < 0.0001)
			return;
		if ( !initialized )
			init( gl );
		
		while (bLocked)
		{
			try
			{
				Thread.sleep( 10 );
			}
			catch ( InterruptedException exc )
			{
				exc.printStackTrace();
			}
		}
		
		JoglGpuContext context = JoglGpuContext.get( gl );
		
		//scale disk with viewport transform
		Vector2f window_sizef =  new Vector2f (screen_size[0], screen_size[1]);
		
		//The whole story behind the code below is that
		//the size of the OpenGL sprite corresponding to a point is
		//changing depending on the actual window size and the render window size parameters.
		//Basically it scales with coefficient screen_size[0]/renderParams.nRenderW (in each dimension).
		//To compensate for that, we have to enlarge (shrink) effective point size
		//(it is done in the vertex shader, we enabled gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE))
		//and then render the point as nice circle by painting it as an ellipse (in the fragment shader)
		//that will scale into the circle %)
		//
		
		Vector2f ellipse_axes = new Vector2f((float)screen_size[0]/(float)BVVSettings.renderWidth, (float)screen_size[1]/(float)BVVSettings.renderHeight);
		
		//scale of viewport vs render
		//we enlarge/shrink to minimum dimension scale
		//and in the ellipse the other dimension will be cropped
		//(maybe this part can be moved to GPU? seems not critical right now)
		
		float fPointScale = Math.min(ellipse_axes.x,ellipse_axes.y);
		ellipse_axes.mul(1.0f/fPointScale);
		
		//actually it is not true ellipse axes,
		//but rather inverse squared values
		ellipse_axes.x = ellipse_axes.x * ellipse_axes.x;
		ellipse_axes.y = ellipse_axes.y * ellipse_axes.y;
				
		prog.getUniformMatrix4f( "pvm" ).set( pvm );
		prog.getUniform1f( "pointSizeReal" ).set( fSpotSize );
		prog.getUniform1f( "pointScale" ).set( fPointScale );
		prog.getUniform4f( "colorin" ).set(l_color);
		prog.getUniform2f( "windowSize" ).set(window_sizef);
		prog.getUniform2f( "ellipseAxes" ).set(ellipse_axes);
		prog.getUniform1i( "renderType" ).set(renderType);
		prog.getUniform1i( "pointShape" ).set( spotShape );
		//progRound.getUniform1i("clipactive").set(0);
		//progRound.getUniform3f("clipmin").set(new Vector3f(BigTraceData.nDimCurr[0][0],BigTraceData.nDimCurr[0][1],BigTraceData.nDimCurr[0][2]));
		//progRound.getUniform3f("clipmax").set(new Vector3f(BigTraceData.nDimCurr[1][0],BigTraceData.nDimCurr[1][1],BigTraceData.nDimCurr[1][2]));
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		if(renderType == RENDER_GAUSS)
		{
			gl.glDepthFunc( GL.GL_ALWAYS);
		}
		else
		{
			gl.glDepthFunc( GL.GL_LESS);

		}
		prog.setUniforms( context );
		
		prog.use( context );
		gl.glBindVertexArray( vao );
		gl.glDrawArrays( GL.GL_POINTS, 0, nSpotsN);
		gl.glBindVertexArray( 0 );
		gl.glDepthFunc( GL.GL_LESS);
	}

}
