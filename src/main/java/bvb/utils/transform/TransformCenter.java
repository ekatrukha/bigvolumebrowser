package bvb.utils.transform;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.FinalRealInterval;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.Source;

import bdv.viewer.SourceToConverterSetupBimap;
import bvb.utils.Misc;


public class TransformCenter
{
	private final SourceToConverterSetupBimap bimap;
	
	private final Map< ConverterSetup, double[]> setupToCenters = new HashMap<>();

	public TransformCenter( final SourceToConverterSetupBimap bimap )
	{
		this.bimap = bimap;
	}
	
	public double[] getCenters( final ConverterSetup setup )
	{
		double [] out =  setupToCenters.get( setup );
		if(out == null)
		{
			out = getDefaultCenters(setup);
			setCenters( setup, out );
		}		
		return out;
	}
	
	public void updateCenters(final ConverterSetup setup)
	{
		setCenters( setup, getDefaultCenters(setup));
	}
	
	public void setCenters( final ConverterSetup setup, final double[] centers)
	{
		setupToCenters.put( setup, centers );
	}
	
	public double [] getDefaultCenters(final ConverterSetup setup)
	{
		final Source< ? > src = bimap.getSource( setup ).getSpimSource();
				
		FinalRealInterval interval = Misc.getSourceBoundingBoxAllTP(src);

		return Misc.getIntervalCenter(interval);		
		
	}
}
