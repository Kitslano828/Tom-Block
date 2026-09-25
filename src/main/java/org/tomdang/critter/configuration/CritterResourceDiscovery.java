package org.tomdang.critter.configuration;
import java.io.IOException;import java.nio.file.*;import java.util.*;import java.util.jar.JarFile;
public final class CritterResourceDiscovery{
	public List<String> discover(Class<?> owner){try{return discover(Path.of(owner.getProtectionDomain().getCodeSource().getLocation().toURI()));}catch(Exception exception){throw new IllegalStateException("Could not locate critter resources",exception);}}
	public List<String> discover(Path source){try{if(Files.isDirectory(source))try(var paths=Files.walk(source.resolve("critters"))){return paths.filter(Files::isRegularFile).map(source::relativize).map(value->value.toString().replace('\\','/')).filter(this::yaml).sorted().toList();}try(var jar=new JarFile(source.toFile())){return jar.stream().filter(value->!value.isDirectory()).map(value->value.getName()).filter(this::yaml).sorted().toList();}}catch(IOException exception){throw new IllegalStateException("Could not discover critters",exception);}}
	private boolean yaml(String path){String value=path.toLowerCase(Locale.ROOT);return value.startsWith("critters/")&&(value.endsWith(".yml")||value.endsWith(".yaml"));}
}
