//redFrik 050703

+Pen {
	
	*mIdentity {
		^[	#[1, 0, 0, 0],
			#[0, 1, 0, 0],
			#[0, 0, 1, 0],
			#[0, 0, 0, 1]];
	}
	*mTranslate {|tx, ty, tz|
		^[	#[1, 0, 0, 0],
			#[0, 1, 0, 0],
			#[0, 0, 1, 0],
			[tx, ty, tz, 1]];
	}
	*mScale {|sX, sY, sZ|
		^[	[sX, 0, 0, 0],
			[0, sY, 0, 0],
			[0, 0, sZ, 0],
			#[0, 0, 0, 1]];
	}
	*mRotateX {|ax|
		^[	#[1, 0, 0, 0],
			[0, cos(ax), sin(ax), 0],
			[0, sin(ax).neg, cos(ax), 0],
			#[0, 0, 0, 1]];
	}
	*mRotateY {|ay|
		^[	[cos(ay), 0, sin(ay).neg, 0],
			#[0, 1, 0, 0],
			[sin(ay), 0, cos(ay), 0],
			#[0, 0, 0, 1]];
	}
	*mRotateZ {|az|
		^[	[cos(az), sin(az), 0, 0],
			[sin(az).neg, cos(az), 0, 0],
			#[0, 0, 1, 0],
			#[0, 0, 0, 1]];
	}
	
	*matrixMatrixMul {|matrix1, matrix2|
		var m0, m1, m2, m3;
		#m0, m1, m2, m3= matrix2;
		^Array.fill(4, {|x|
			Array.fill(4, {|y|
				(matrix1[x][0]*m0[y])+
				(matrix1[x][1]*m1[y])+
				(matrix1[x][2]*m2[y])+
				(matrix1[x][3]*m3[y])
			});
		});
	}
	*vectorMatrixMul {|vector, matrix|
		var v0, v1, v2, m0, m1, m2, m3;
		#v0, v1, v2= vector;
		#m0, m1, m2, m3= matrix;
		^[
			(v0*m0[0])+(v1*m1[0])+(v2*m2[0])+m3[0],
			(v0*m0[1])+(v1*m1[1])+(v2*m2[1])+m3[1],
			(v0*m0[2])+(v1*m1[2])+(v2*m2[2])+m3[2]
		];
	}
}
