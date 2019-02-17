package com.zmc.robot.fxrobotui;

import javafx.scene.paint.Color;


public class ComponentInfo {
	
	
	public ComponentInfo(){
		
	}
	
	public ComponentInfo(Color color, double path[][] )
	{
		this.color  = color;
		this.path = path;
	}
	
	
	public ComponentInfo clone()
	{
		ComponentInfo info = new ComponentInfo(color, copyArray(path));
		return info;
	}
	
	public ComponentInfo offset(double x, double y)
	{
		 
		if( path == null )
			return null;
		
		double[][] path  = copyArray( this.path );
		
		int len1 = path.length;
		for( int i=0; i<len1; i++)
		{
			path[i][0] = x + path[i][0];
			path[i][1] = y + path[i][1];
		}
		
		ComponentInfo component = new ComponentInfo(this.color, path);
		return component;
		
	}
	
	public ComponentInfo transform(double x, double y, double theta )
	{
		
		double cosTheta = Math.cos(theta);
		double sinTheta = Math.sin( theta );
		
		if( path == null )
			return null;

		double[][] path1 = copyArray( this.path );
		
		int len1 = path1.length;
		double x1,y1;
		for( int i=0; i<len1; i++)
		{
			x1 = x + path1[i][0] * cosTheta + path1[i][1] * sinTheta;
			y1 = y - path1[i][0] *sinTheta + path1[i][1] * cosTheta;
			path1[i][0] = x1;
			path1[i][1] = y1;
			
		}
		
		ComponentInfo component = new ComponentInfo(this.color, path1);
		return component;
		
	}
	
	private double[][] copyArray(double src[][] )
	{
		if( src == null )
			return null;
		int len1 = src.length;
		int len2 = src[0].length;
		
		double[][] dst = new double[len1][len2];
		
		for( int i=0; i<len1; i++)
			for(int j=0; j<len2; j++)
				dst[i][j] = src[i][j];
		
//		System.arraycopy(src, 0, dst, 0,  *len1*len2);
		return dst ;
		
		
	}
	
	public double path[][];
	public Color color;	
	
	
	
}
