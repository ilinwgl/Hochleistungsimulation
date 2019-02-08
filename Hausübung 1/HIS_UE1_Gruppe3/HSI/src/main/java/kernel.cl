__kernel
void sampleKernel(__global const float *a, __global const float *b, __global float *c,     
 const int Ndim, const int Mdim)
 {
	 int row = get_global_id(0);
	 int local_work_size = get_local_size(0);
	 float tmp = 0.0f;
	if (row < Ndim){
	 for (int k=0; k < Mdim; k++){
		 tmp += a[row * Mdim + k] * b[k];
		 c[row] = tmp;
		 }
 }
  if(row == 0){ printf ("Local work size: %d \n", local_work_size);}
 };
		 
