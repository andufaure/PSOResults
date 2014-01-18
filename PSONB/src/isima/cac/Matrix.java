/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package isima.cac;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * 2D Matrix
 * @author onio
 */
public class Matrix {
    
    private int    width = -1;
    private int    height = -1;
    private byte[] matrix = null;
    
    public static Matrix buildFromCSV(String inFilename) throws Exception {
        
        Matrix  matrix = null;
        File    file = new File(inFilename);
        Scanner scanner = new Scanner(file);
        int     h = 0;
        int     w = 0;
        boolean ld_instance = false;
        
        scanner.useDelimiter(",");
        
        w = scanner.nextInt();
        h = scanner.nextInt();
        scanner.useDelimiter("");
        
        scanner.nextLine();
        
        
        String  str = scanner.nextLine();
        StringTokenizer tokenizer = new StringTokenizer(str, ",");
        
        ld_instance = ((new Byte(tokenizer.nextToken())).byteValue() == 9);
        
        scanner.close();
        
        scanner = new Scanner(file);
        scanner.nextLine();
        //System.out.println(scanner.nextLine());
        
        if (ld_instance)
        {            
            matrix = new Matrix(w - 2, h - 2);

            for (int i = 0 ; i < h ; i++)
            {
                str = scanner.nextLine();
                //System.out.println(str);
                
                if (i == 0 || i == (h - 1))
                    continue;

                tokenizer = new StringTokenizer(str, ",");

                for (int j = 0 ; j < w ; j++)
                {
                    byte b = new Byte(tokenizer.nextToken()).byteValue();

                    if (j == 0 || j == (w - 1))
                        continue;

                    matrix.set(j - 1, i - 1, b);
                }
                //System.out.println("I :" + i + " : " + scanner.nextLine());
            }
        }
        else
        {            
            matrix = new Matrix(w, h);
            
            for (int i = 0 ; i < h ; i++)
            {
                str = scanner.nextLine();
                tokenizer = new StringTokenizer(str, ",");

                for (int j = 0 ; j < w ; j++)
                {
                    byte b = new Byte(tokenizer.nextToken()).byteValue();
                    matrix.set(j, i, b);
                }
                //System.out.println("I :" + i + " : " + scanner.nextLine());
            }
        }
        
        //System.out.println(matrix.toString());
        scanner.close();
        return (matrix);
    }
    

    public static int cellDiffBetween(Matrix m1, Matrix m2) throws Exception
    {
        int cnt = 0;
        
        if (m1.width != m2.width || m1.height != m2.height)
            throw new Exception("matrices must have same size");
        
        for (int i = 0 ; i < m1.width * m2.height ; i++)
            cnt += (m1.matrix[i] == m2.matrix[i]) ? 0 : 1;
        
        return cnt;
    }
    
    public Matrix(int w, int h) throws Exception
    {
        if (w <= 0 || h <= 0)
            throw new Exception("w & h must be > 0");
        
        this.width = w;
        this.height = h;
        matrix = new byte[w * h];
        Arrays.fill(matrix, (byte)0);
    }
    
    public Matrix(Matrix inMatrix)
    {
        this.width = inMatrix.width;
        this.height = inMatrix.height;
        this.matrix = new byte[this.width * this.height];
        System.arraycopy(inMatrix.matrix, 0, this.matrix, 0, width * height);
    }
    
    public void set(int x, int y, byte value) throws Exception
    {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
            return;
        
        this.matrix[width * (Math.abs(y) % height) + (Math.abs(x) % width)] = (byte)(value & 1);
    }
    
    public byte get(int x, int y) throws Exception
    {
        
        
        return (this.matrix[width * (Math.abs(y) % height) + (Math.abs(x) % width)]);
    }
    
    public Matrix buildCopy()
    {
        return (new Matrix(this));
    }
    
    public void   copyContentFrom(Matrix inMatrix) throws Exception
    {   
        if (this.width != inMatrix.width || this.height != inMatrix.height)
            throw new Exception("width & height not correct");
        
        System.arraycopy(inMatrix.matrix, 0, this.matrix, 0, width * height);
    }
    
    public int getHeight() {
        return height;
    }

    public byte[] getMatrix() {
        return matrix;
    }

    public int getWidth() {
        return width;
    }
    
    public void print() throws Exception {
        
        for (int i = 0 ; i < height ; i++)
        {
            for (int j = 0 ; j < width ; j++)
                System.out.print(this.get(j, i));
            System.out.println("");
        }
    }
    
    public void completeMutat()
    {
        for (int i = 0 ; i < height * width ; i++)
            matrix[i] = (byte)((matrix[i] ^ 1) & 1);
    }
}
