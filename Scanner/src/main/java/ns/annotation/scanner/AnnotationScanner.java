package ns.annotation.scanner;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;

public class AnnotationScanner {
	
	public static void main(String[] args) {
		String packageName = "ns.annotation.scanner";
		annotationScanner(packageName, TestAnnotation.class.getName());
	}
	
	/**
	 * AnnotationScanner method consumes packageName and annotationTypeName
	 * and detects classes who has under the given package name and has given annotationTypeName
	 *
	 * @param packageName 			The given base package
	 * @param annotationTypeName 	The given Annotation Type Name
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static void annotationScanner(String packageName, String annotationTypeName) {
		try {
			List<File> classes = getClasses(packageName);
			if (classes != null && !classes.isEmpty()) {
				InputStream classInputStream = null;
				DataInputStream classDataStream = null;
				ClassFile classFile = null;
				AnnotationsAttribute visible = null;
				for (File file : classes) {
					classInputStream = new FileInputStream(file);
					classDataStream = new DataInputStream(new BufferedInputStream(classInputStream));
					classFile = new ClassFile(classDataStream);
					visible = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
					if (visible != null) {
						for (Annotation ann : visible.getAnnotations()) {
							if (annotationTypeName.equals(ann.getTypeName())) {
								System.out.println("ClassName      : " + classFile.getName());
								System.out.println("AnnotationName : " + ann.getTypeName());	
							}
						}
					}
				}
			}
        } catch (IOException ioe) {
           ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
        	cnfe.printStackTrace();
		} catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	 /**
	 * Scans all classes accessible from the context class loader which belong to the given package and sub packages.
	 *
	 * @param packageName The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private static List<File> getClasses(String packageName) throws ClassNotFoundException, IOException {
	    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	    assert classLoader != null;
	    String path = packageName.replace('.', '/');
	    Enumeration<URL> resources = classLoader.getResources(path);
	    List<File> dirs = new ArrayList<File>();
	    while (resources.hasMoreElements()) {
	        URL resource = resources.nextElement();
	        dirs.add(new File(resource.getFile()));
	    }
	    ArrayList<File> classes = new ArrayList<>();
	    for (File directory : dirs) {
	        classes.addAll(findClasses(directory, packageName));
	    }
	    return classes;
	}
	
	/**
	 * Recursive method used to find all classes in a given directory and sub directories.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List<File> findClasses(File directory, String packageName) throws ClassNotFoundException {
	    List<File> classes = new ArrayList<File>();
	    if (!directory.exists()) {
	        return classes;
	    }
	    File[] files = directory.listFiles();
	    for (File file : files) {
	        if (file.isDirectory()) {
	            assert !file.getName().contains(".");
	            classes.addAll(findClasses(file, packageName + "." + file.getName()));
	        } else if (file.getName().endsWith(".class")) {
	            classes.add(file);
	        }
	    }
	    return classes;
	}
	
}
