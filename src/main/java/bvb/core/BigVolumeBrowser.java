package bvb.core;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.imglib2.FinalRealInterval;

import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.SpimDataException;

import ij.ImageJ;
import ij.plugin.PlugIn;

import bvvpg.core.VolumeViewerFrame;
import bvvpg.core.VolumeViewerPanel;
import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvHandleFrame;
import bvvpg.vistools.BvvStackSource;

import bvb.gui.BVBControlPanel;
import bvb.io.BDVHDF5Loader;


public class BigVolumeBrowser  implements PlugIn
{
	/** Bvv instance **/
	public Bvv bvv = null;
	
	/** Panel of BigVolumeViewer **/
	public VolumeViewerPanel bvvViewer;

	/** Frame of BigVolumeViewer **/
	public VolumeViewerFrame bvvFrame;
	
	/** control panel **/
	public BVBControlPanel controlPanel;
	
	/** actions and behaviors **/
	public BVBActions bvbActions;
	
	@SuppressWarnings( "rawtypes" )
	private final ConcurrentHashMap < BvvStackSource<?>, AbstractSpimData > bvvSourceToSpimData;
	
	@SuppressWarnings( "rawtypes" )
	private final ConcurrentHashMap < AbstractSpimData, List<BvvStackSource<?> >> spimDataTobvvSourceList;
	
	public BigVolumeBrowser()
	{
		bvvSourceToSpimData = new ConcurrentHashMap<>();
		spimDataTobvvSourceList = new ConcurrentHashMap<>();
		
	}
	/** starting as plugin from ImageJ/FIJI **/
	@Override
	public void run( String arg )
	{
		
		startBVB();
		bvbActions = new BVBActions(this);
	}
	
	
	public void startBVB()
	{
		//switch to FlatLaf theme		
		try {
		    UIManager.setLookAndFeel( new FlatIntelliJLaf() );
		    FlatLaf.registerCustomDefaultsSource( "flatlaf" );
		    FlatIntelliJLaf.setup();
		} catch( Exception ex ) {
		    System.err.println( "Failed to initialize LaF" );
		}
		
		if(bvv == null)
		{
			//start empty bvv
			bvv = BvvFunctions.show( Bvv.options().
					dCam(BVBSettings.dCam).
					dClipNear(BVBSettings.dClipNear).
					dClipFar(BVBSettings.dClipFar).				
					renderWidth( BVBSettings.renderWidth).
					renderHeight( BVBSettings.renderHeight).
					numDitherSamples( BVBSettings.numDitherSamples ).
					cacheBlockSize( BVBSettings.cacheBlockSize ).
					maxCacheSizeInMB( BVBSettings.maxCacheSizeInMB ).
					ditherWidth(BVBSettings.ditherWidth).
					frameTitle("BigVolumeBrowser")
					);
			bvvViewer = ((BvvHandleFrame)bvv.getBvvHandle()).getViewerPanel();
			bvvFrame = ((BvvHandleFrame)bvv.getBvvHandle()).getBigVolumeViewer().getViewerFrame();
			
			bvvFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			//setup control panel
			
			controlPanel = new BVBControlPanel(this);
			controlPanel.cpFrame = new JFrame("BVB Control Panel");
			controlPanel.cpFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			controlPanel.cpFrame.add(controlPanel);
			
	        //Display the window.
			controlPanel.cpFrame.setSize(400,600);
			controlPanel.cpFrame.setVisible(true);
		    java.awt.Point bvv_p = bvvFrame.getLocationOnScreen();
		    java.awt.Dimension bvv_d = bvvFrame.getSize();
		
		    controlPanel.cpFrame.setLocation(bvv_p.x + bvv_d.width, bvv_p.y);
		    
		    //sync closing
		    final WindowAdapter closeWA = new WindowAdapter()
			{
				@Override
				public void windowClosing( WindowEvent ev )
				{
					closeWindows();
				}
			};
			
			controlPanel.cpFrame.addWindowListener( closeWA );
		    bvvFrame.addWindowListener(	closeWA );
		}
	}
	
	public void closeWindows()
	{
		bvvViewer.stop();
		bvvFrame.dispose();		
		controlPanel.cpFrame.dispose();
	}
	
	@SuppressWarnings( "rawtypes" )
	public void loadBDVHDF5(String xmlFileName)
	{
		AbstractSpimData spimData;
		try
		{
			spimData = BDVHDF5Loader.loadHDF5( xmlFileName );
			if(bvv == null)
			{
				startBVB();
			}
			List< BvvStackSource< ? > > sourcesSPIM = BvvFunctions.show(spimData, Bvv.options().addTo( bvv ));
			
			spimDataTobvvSourceList.put( spimData, sourcesSPIM );
			for (BvvStackSource< ? > bvvSource : sourcesSPIM) 
			{
				bvvSourceToSpimData.put( bvvSource, spimData );
			}
//			double [] minI = new double[3];
//			double [] maxI = new double[3];
//			for(int i=0;i<3;i++)
//				maxI[i]=30;
//			sourcesSPIM.get( 0 ).setClipInterval(new FinalRealInterval(minI,maxI)  );
			
//			double [] minI = new double[3];
//			double [] maxI = new double[3];
//			for(int i=0;i<2;i++)
//			{
//				minI[i] = 225;
//				maxI[i] = 275;
//			}
//			minI[2] = 200;
//			maxI[2] = 300;
//
//			sourcesSPIM.get( 0 ).setClipInterval(new FinalRealInterval(minI,maxI)  );	
//			sourcesSPIM.get( 1 ).setClipInterval(new FinalRealInterval(minI,maxI)  );	
//			sourcesSPIM.get( 0 ).setLUT( "Green" );
//			sourcesSPIM.get( 1 ).setLUT( "Red" );	
		}
		catch ( SpimDataException exc )
		{
			exc.printStackTrace();
		}
		
		
		
	}
	
	public static void main(String... args) throws Exception
	{
		
		new ImageJ();
		BigVolumeBrowser testBVB = new BigVolumeBrowser(); 
		
		testBVB.run("");
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/whitecube_2ch.xml" );

		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/ExM_MT.xml" );
		testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/2_channels.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/HyperStack.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/trace1514947168.xml" );
		//testBVB.loadBDVHDF5( "/home/eugene/Desktop/projects/BVB/cliptest.xml" );
	}
}
