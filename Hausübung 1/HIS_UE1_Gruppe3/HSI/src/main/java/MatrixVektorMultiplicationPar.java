/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */


import static org.jocl.CL.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.jocl.*;


public class MatrixVektorMultiplicationPar
{
    /**
     * The source code of the OpenCL program to execute
     */
	private static String programSource;

	    public static void main( String[] args )
	    {
	       //create input and output data 
	        int m = 10000;
	        int n = m;
	        float A[] = new float[m*n]; 
	        float b[] = new float[n];
	        float c[] = new float[m];
	        for (int i = 0; i<m ; i++)
	        {
	        	for (int j = 0; j<n; j++)
	        	{
	        		b[j] = j;
	        		A[n*i+j] = n*i+j;
	        	}
	        }
	        
	        Pointer srcA = Pointer.to(A); 
	        Pointer srcb = Pointer.to(b);
	        Pointer srcc = Pointer.to(c);
	        
	        // Read kernel from file:
	        programSource = readFile("C:\\Users\\key\\Desktop\\HSI(4)\\HSI\\src\\main\\java\\kernel.cl");
	        //System.out.println( programSource );

	        //platform, device type and device number 
	        final int platformIndex = 0;
	        final int deviceIndex = 0;
	        final long deviceType = CL_DEVICE_TYPE_ALL;
	        
	        // Enable exceptions and subsequently omit error checks in this sample
	        CL.setExceptionsEnabled(true);
	        
	        //Obtain the number of platforms
	        int numPlatformsArray[] = new int[1];
	        clGetPlatformIDs(0, null, numPlatformsArray);
	        int numPlatforms = numPlatformsArray[0];
	        
	        //Obtain the ID of platforms
	        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
	        clGetPlatformIDs(platforms.length, platforms, null);
	        cl_platform_id platform = platforms[platformIndex];
	        
	        //Initialize the context properties
	        cl_context_properties contextProperties = new cl_context_properties();
	        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
	        
	        //Obtain the number of device
	        int numDevicesArray[] = new int[1];
	        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
	        int numDevices = numDevicesArray[0];
	        
	        //Obtain the ID of Device
	        cl_device_id devices[] = new cl_device_id[numDevices];
	        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
	        cl_device_id device = devices[deviceIndex];
	        	
	        //Create a context for the selected device
	        cl_context context = clCreateContext(contextProperties, devices.length, devices, null, null, null);
	      
	        // Create the program from the source code
	        cl_program program = clCreateProgramWithSource(context,
	            1, new String[]{ programSource }, null, null);
	        
	        // Build the program
	        clBuildProgram(program, 0, null, null, null, null);
	        
	        // Create the kernel
	        cl_kernel kernel = clCreateKernel(program, "sampleKernel", null);
	        
	        //Allocate the memory objects for the input- and output data
	        cl_mem memObjects[] = new cl_mem[3];
	        memObjects[0] = clCreateBuffer(context, 
	            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * n*m, srcA, null);
	        memObjects[1] = clCreateBuffer(context, 
	            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
	            Sizeof.cl_float * n, srcb, null);
	        memObjects[2] = clCreateBuffer(context, 
	            CL_MEM_READ_WRITE, 
	            Sizeof.cl_float * m, null, null);
	        
	        //Create a command-queue for the selected device
	        cl_command_queue commandQueues[] = new cl_command_queue[numDevices];
	        long properties = 0;
	        properties |= CL.CL_QUEUE_PROFILING_ENABLE;
	        for (int i=0; i<numDevices; i++)
	        {
	            commandQueues[i] = clCreateCommandQueue(
	                context, devices[i], properties, null);
	        }
	        
	        // Set the work-item dimensions
	        long global_work_size[] = new long[]{m};
	        long local_work_size[] = new long[]{40};
	        
	        // Execute the kernel on each command queue, and 
	        // create events for each kernel launch
	        long before = System.nanoTime();
	        System.out.println("Enqueueing kernels");
	        cl_event events[] = new cl_event[numDevices];
	        for (int i=0; i<numDevices; i++)
	        {
	            clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
	            clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
	            clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
                clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{m}));
                clSetKernelArg(kernel, 4, Sizeof.cl_int, Pointer.to(new int[]{n}));
                
	            events[i] = new cl_event();
	            clEnqueueNDRangeKernel(commandQueues[i], kernel, 1, null,
	                global_work_size, local_work_size, 0, null, events[i]);
	        }
	        
	        // Wait until the work is finished on all command queues
	        System.out.println("Waiting for kernels");
	        clWaitForEvents(events.length, events);
	        long after = System.nanoTime();
	        
	        // Print the duration for each device
	        System.out.println("Waiting for kernels DONE");
	        for (int i=0; i<numDevices; i++)
	        {
	            float durationMs = computeDurationMs(events[i]);
	            System.out.println("Duration on device "+i+" of "+
	                numDevices+": "+durationMs+"ms");
	        }
	        float totalDurationMs = (after-before)/1e6f;
	        System.out.println("Total duration: "+totalDurationMs+"ms");
	        
	        // Read the output data
	        clEnqueueReadBuffer(commandQueues[0], memObjects[2], CL_TRUE, 0,
	            m* Sizeof.cl_float, srcc, 0, null, null);
	        
	        // Release kernel, program, and memory objects
	        clReleaseMemObject(memObjects[0]);
	        clReleaseMemObject(memObjects[1]);
	        clReleaseMemObject(memObjects[2]);
	        clReleaseKernel(kernel);
	        clReleaseProgram(program);
	        clReleaseContext(context);
	        for (int i=0; i<numDevices; i++)
	        {
	            clReleaseEvent(events[i]);
	            clReleaseCommandQueue(commandQueues[i]);
	        }

	        if (m <= 10 && n <= 10)
	        {
	            System.out.println("Result: "+java.util.Arrays.toString(c));
	        }
	        
	        /**
	         * Compute the execution duration of the given event, in milliseconds
	         * 
	         * @param event The event
	         * @return The execution duration, in milliseconds
	         */

	    }
	    private static float computeDurationMs(cl_event event)
	    {
	        long startTime[] = {0};
	        long endTime[] = {0};
	        CL.clGetEventProfilingInfo(
	            event, CL.CL_PROFILING_COMMAND_START,
	            Sizeof.cl_ulong, Pointer.to(startTime), null);
	        CL.clGetEventProfilingInfo(
	            event, CL.CL_PROFILING_COMMAND_END,
	            Sizeof.cl_ulong, Pointer.to(endTime), null);
	        long durationNs = endTime[0]-startTime[0];
	        return durationNs / 1e6f;
	    }
	    
	    private static String readFile(String fileName)
	    {
	        try
	        {
	            BufferedReader br = new BufferedReader(new FileReader(fileName));
	            StringBuilder sb = new StringBuilder();
	            String line = null;
	            while (true)
	            {
	                line = br.readLine();
	                if (line == null)
	                {
	                    break;
	                }
	                sb.append(line+"\n");
	            }
	            return sb.toString();
	        }
	        catch (IOException e)
	        {
	            e.printStackTrace();
	            return "";
	        }
	    }
}