/* (C) 2021 Péter Varkoly <pvarkoly@cephalix.eu> Nuremberg Germany - all rights reserved
* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved
* */
package de.cranix.services;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;

import de.cranix.helper.CrxSystemCmd;
import static de.cranix.helper.CranixConstants.*;

public class Config {
	private Path CRX_CONFIG = Paths.get(cranixSysConfig);
	private String prefix = cranixSysPrefix;
	private Map<String,String>   config;
	private Map<String,String>   configPath;
	private Map<String,String>   configType;
	private Map<String,String>   configHelp;
	private Map<String,Boolean>  readOnly;
	private List<String>         configFile;
	protected Map<String, String> properties;

	public Config() {
		this.InitConfig();
	}

	public Config(String configPath, String prefix) {
		this.CRX_CONFIG = Paths.get(configPath);
		this.prefix = prefix;
		this.InitConfig();
	}

	public void setConfig(String configPath, String prefix) {
		this.CRX_CONFIG = Paths.get(configPath);
		this.prefix = prefix;
		this.InitConfig();
	}
	public void InitConfig() {
		config     = new HashMap<>();
		readOnly   = new HashMap<>();
		configPath = new HashMap<>();
		configType = new HashMap<>();
		configHelp = new HashMap<>();
		properties = new HashMap<String, String>();
		try {
			configFile = Files.readAllLines(this.CRX_CONFIG);
			File file = new File(cranixPropFile);
			FileInputStream fileInput = new FileInputStream(file);
			Properties props = new Properties();
			props.load(fileInput);
			fileInput.close();
			Enumeration<Object> enuKeys = props.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = props.getProperty(key);
				properties.put(key, value);
			}
		} catch( IOException e ) {
			e.printStackTrace();
			return;
		}
		Boolean ro = false;
		String  path = "Backup";
		String  type = "string";
		StringBuilder help = new StringBuilder();
		for ( String line : configFile ){
			if( line.startsWith("#") && line.contains("readonly")) {
				ro = true;
			}
			if( line.startsWith("## Path:")) {
				String[] l = line.split("\\/");
				if( l.length == 3 ) {
				  path = l[2];
				}
			}
			if( line.startsWith("## Type:")) {
				String[] l = line.split("\\s+");
				if( l.length >= 3 ) {
				  type = l[2];
				}
			}
			if( line.startsWith("# ")) {
				help.append(line.substring(1));
			}
			if( !line.startsWith("#") ) {
				String[] sline = line.split("=", 2);
				if( sline.length == 2 )
				{
					String key   = sline[0].substring(this.prefix.length());
					String value = sline[1];
					if( value.startsWith("\"") || value.startsWith("'") ){
						value = value.substring(1);
					}
					if( value.endsWith("\"") || value.endsWith("'") ){
						value = value.substring(0,value.length()-1);
					}
					readOnly.put(key,  ro);
					config.put(key,    value);
					configPath.put(key,path);
					configType.put(key,type);
					configHelp.put(key,help.toString());
					ro = false;
				}
				help = new StringBuilder();
			}
		}
	}

	public String getProperty(String property) {
		if (properties.containsKey(property)) {
			return properties.get(property);
		}
		return "";
	}

	public Date now() {
		return new Date(System.currentTimeMillis());
	}

	public String nowString() {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss");
		return  fmt.format(new Date());
	}

	public String nowDateString() {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		return  fmt.format(new Date());
	}

	public Boolean isConfgiReadOnly(String key){
		return readOnly.get(key);
	}

	public String getConfigValue(String key){
		if( config.containsKey(key)) {
			return config.get(key);
		} else {
			return "";
		}
	}

	public String getConfigPath(String key){
		return configPath.get(key);
	}

	public String getConfigType(String key){
		return configType.get(key);
	}

	public List<String> getConfigPaths() {
 		List<String> paths = new ArrayList<String>();
		for ( String path : configPath.values() )
		{
		   if(!paths.contains(path))
			   paths.add(path);
		}
		return paths;
	}

	public List<String> getKeysOfPath(String path) {
		List<String> keys = new ArrayList<String>();
		for ( String key : configPath.keySet() ) {
			if( configPath.get(key).startsWith(path) )
			  keys.add(key);
		}
		Collections.sort(keys);
		return keys;
	}

	public Boolean setConfigValue(String key, String value){
		Boolean ro = readOnly.get(key);
		if(ro!=null && ro.booleanValue()){
			return false;
		}
		config.put(key, value);
		List<String> tmpConfig =  new ArrayList<String>();
		for ( String line : configFile ){
			if(line.startsWith(this.prefix + key + "=")){
				tmpConfig.add( this.prefix + key + "=\"" + value + "\"" );
			}
			else{
				tmpConfig.add( line );
			}
		}
		configFile = tmpConfig;
		try {
			Files.write(this.CRX_CONFIG, tmpConfig );
		}
		catch( IOException e ) {
			e.printStackTrace();
			return false;
		}
		//Start plugin modify_config
		StringBuilder data = new StringBuilder();
		String[] program   = new String[2];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = cranixBaseDir + "plugins/plugin_handler.sh";
		program[1] = "modify_config";
		data.append(String.format("key: %s%n", key));
		data.append(String.format("value: %s%n", value));
		int ret = CrxSystemCmd.exec(program, reply, error, data.toString());
		return true;
	}

	public List<Map<String,String>> getConfig() {
		List<Map<String, String>> configs = new ArrayList<>();
		for( String key : config.keySet() ){
			Map<String,String> configMap = new HashMap<>();
			configMap.put("key",      key);
			configMap.put("path",     configPath.get(key));
			configMap.put("value",    config.get(key));
			configMap.put("type",     configType.get(key));
			configMap.put("readOnly", readOnly.get(key) ? "yes" : "no" );
			configMap.put("help",     configHelp.get(key));
			configs.add(configMap);
		}
		return configs;
	}
}
