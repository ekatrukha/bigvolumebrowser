uniform int dotted;
uniform vec4 color;
uniform float antialias;
uniform float thickness;
uniform float linelength;
in vec2 v_uv;

in vec3 posW;
uniform vec3 clipmin;
uniform vec3 clipmax;
uniform int clipactive;
out vec4 fragColor;

void main()
{
    //ROI clipping
	if(clipactive>0)
	{
		vec3 s = step(clipmin, posW) - step(clipmax, posW);
		if(s.x * s.y * s.z == 0.0)
		{
			discard;
		}
	}
	
	// drawing of antialiased 3D lines
	// taken and adapted from 
	// https://www.labri.fr/perso/nrougier/python-opengl/#d-lines
	// Python & OpenGL for Scientific Visualization
	// Copyright (c) 2018 - Nicolas P. Rougier <Nicolas.Rougier@inria.fr>
	
    float d = 0;
    float w = thickness/2.0 - antialias;

    vec4 colorOut = vec4(color);

	if(dotted == 0 )
	{
	    // Cap at start
	    if (v_uv.x < 0)
	    {      
	       d = length(v_uv) - w;
	    }
	    // Cap at end
	    else if (v_uv.x >= linelength)
	    {
			d = length(v_uv - vec2(linelength,0)) - w;
	    }
	    // Body
	    else
	    {
	        d = abs(v_uv.y) - w;
	    }
	}
	else
	{
	    float spacing = 2.5;
	    float phase = 0.0;
	    float center = v_uv.x + spacing/2.0*thickness
	                 - mod(v_uv.x + phase + spacing/2.0*thickness, spacing*thickness);
	    if (linelength - center < thickness/2.0)
	        discard;
	    else if (center < thickness/2.0)
	        discard;
	    else
	        d = length(v_uv - vec2(center,0.0)) - w;
	}
        
    if( d < 0) 
    {
       fragColor = vec4(color);     
    } 
    else 
    {
        d /= antialias;
        fragColor = vec4(color.xyz, color.a*exp(-d*d));
    }
}
