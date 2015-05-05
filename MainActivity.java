package ca.uwaterloo.lab1_201_04;

import java.util.Arrays;

import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity{
	
	static LineGraphView graph;
	static int stepCount;
	static float[] rotationValues;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
            stepCount = 0; 
        }
        else {
        	stepCount = savedInstanceState.getInt("Step_Count");
        }
        
		graph = new LineGraphView(getApplicationContext(), 100, Arrays.asList("x","y","z"));
		graph.setVisibility(View.VISIBLE);
		
    }

    @Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
    	outState.putInt("Step_Count", stepCount);
		super.onSaveInstanceState(outState);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    
    public static class PlaceholderFragment extends Fragment {
    	
    	public PlaceholderFragment() {
    	}
    	
    	@Override
    	public View onCreateView(LayoutInflater inflater, ViewGroup container,
    	Bundle savedInstanceState) {
    		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
    		
    		TextView tv = (TextView) rootView.findViewById(R.id.label1);
    		tv.setText("Pedometer");
    		tv.setTextSize(24f);
    		
    		TextView title = new TextView(rootView.getContext());
    		title.setText("Lab 2 | Section 201 | Group 04");
    		
    		TextView linearAccelerometer = new TextView(rootView.getContext());
    		linearAccelerometer.setTypeface(null, Typeface.BOLD);
       		
    		Button resetButton = new Button(rootView.getContext());
    		resetButton.setText("Reset Count");
    		
    		resetButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    stepCount = 0;
                }
            });
    		
       		LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.layout1);
    		layout.setOrientation(LinearLayout.VERTICAL);
    		
    		layout.addView(title);
    		layout.addView(linearAccelerometer);
    		layout.addView(graph);
    		layout.addView(resetButton);
    		
    		SensorManager sensorManager = (SensorManager) rootView.getContext().getSystemService(SENSOR_SERVICE);
    		
    		Sensor linearAccelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    		Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    		
    		SensorEventListener linearAccelerometerSensorListener = new AccelerometerEventListener(linearAccelerometer);
    		SensorEventListener rotationSensorListener = new RotationEventListener();
    		
    		sensorManager.registerListener(rotationSensorListener, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
    		sensorManager.registerListener(linearAccelerometerSensorListener, linearAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);

    		return rootView;
    	}
    	
    	class RotationEventListener implements SensorEventListener {
    		
    		public RotationEventListener() {
    			
    		}
    		
			@Override
			public void onSensorChanged(SensorEvent se) {
				if (se.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
					rotationValues = se.values;
				}
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub
				
			}
    	}
    	
    	class AccelerometerEventListener implements SensorEventListener {
        	TextView output;
        	float[] store;
        	float previousOutputz;
        	boolean highPoint, lowPoint, endPoint;
        	double z;
        	final float alpha = 0.35f;
        	
        	public AccelerometerEventListener(TextView outputView)	{
        		output = outputView;
            	store = new float [3];
        	}
        	
        	
        	public void onAccuracyChanged(Sensor s, int i){
        		
        	}
        	
        	public void onSensorChanged(SensorEvent se){
        		if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
        			
        			store [2] = store [1];
        			store [1] = store [0];
        			store [0] = se.values[2];
  
        			z = alpha * store[0] + (1 - alpha) * previousOutputz;
        			previousOutputz = (float) z;
        			
        			float[] myArray = { (float) z };
        			
        			graph.addPoint(myArray);
        			        			
        			if ( z > 1 && Math.abs(se.values[1]) > 1.5 && Math.abs(rotationValues[0]) < 2.5 && Math.abs(rotationValues[1]) < 2.5) 
        			{
        					highPoint = true;
        			}
        			else if ( z < -.5 && highPoint && Math.abs(rotationValues[0]) < 2.5 && Math.abs(rotationValues[1]) < 2.5) 
        			{
        					lowPoint = true;
        			}
        			else if ( z < 1 && z > -.5 && highPoint && lowPoint && Math.abs(rotationValues[0]) < 2.5 && Math.abs(rotationValues[1]) < 2.5)
        			{
        					endPoint = true;
        			}

        			if( Math.abs(rotationValues[0]) > 5 || Math.abs(rotationValues[1]) > 5) 
        			{
        				highPoint = lowPoint = endPoint=  false;
        			}
        			
        			if (highPoint && lowPoint && endPoint )
        			{
        				stepCount++;
        				highPoint = lowPoint = endPoint = false;
        			}
        			
        			output.setText("Step Count: " + stepCount);
        		}
        	}
        }
	}

}
