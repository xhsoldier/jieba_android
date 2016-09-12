package com.analysis.jieba;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class WordDictionary {
    private static WordDictionary singleton;
    private static final String MAIN_DICT = "/resources/dict.txt";
    private static String USER_DICT_SUFFIX = ".dict";

    public final Map<String, Double> freqs = new HashMap<String, Double>();
    public final Set<String> loadedPath = new HashSet<String>();
    private Double minFreq = Double.MAX_VALUE;
    private Double total = 0.0;
    private DictSegment _dict;


    private WordDictionary() {
        this.loadDict();
    }


    public static WordDictionary getInstance() {
        if (singleton == null) {
            synchronized (WordDictionary.class) {
                if (singleton == null) {
                    singleton = new WordDictionary();
                    return singleton;
                }
            }
        }
        return singleton;
    }


    /**
     * for ES to initialize the user dictionary.
     * 
     * @param configFilePath
     */
    public void init(String configFilePath) {
        System.out.println("initialize user dictionary:" + configFilePath);
        synchronized (WordDictionary.class) {
            if (loadedPath.contains(configFilePath))
                return;

            singleton.loadUserDict(configFilePath);
            loadedPath.add(configFilePath);
        }
    }
    
    
    /**
     * let user just use their own dict instead of the default dict
     */
    public void resetDict(){
    	_dict = new DictSegment((char) 0);
    	freqs.clear();
    }


    public void loadDict() {
        _dict = new DictSegment((char) 0);
        InputStream is = this.getClass().getResourceAsStream(MAIN_DICT);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

            long s = System.currentTimeMillis();
            while (br.ready()) {
                String line = br.readLine();
                String[] tokens = line.split("[\t ]+");

                if (tokens.length < 2)
                    continue;

                String word = tokens[0];
                double freq = Double.valueOf(tokens[1]);
                total += freq;
                word = addWord(word);
                freqs.put(word, freq);
            }
            // normalize
            for (Entry<String, Double> entry : freqs.entrySet()) {
                entry.setValue((Math.log(entry.getValue() / total)));
                minFreq = Math.min(entry.getValue(), minFreq);
            }
            System.out.println(String.format(Locale.getDefault(), "main dict load finished, time elapsed %d ms",
                System.currentTimeMillis() - s));
        }
        catch (IOException e) {
            System.err.println(String.format(Locale.getDefault(), "%s load failure!", MAIN_DICT));
        }
        finally {
            try {
                if (null != is)
                    is.close();
            }
            catch (IOException e) {
                System.err.println(String.format(Locale.getDefault(), "%s close failure!", MAIN_DICT));
            }
        }
    }


    private String addWord(String word) {
        if (null != word && !"".equals(word.trim())) {
            String key = word.trim().toLowerCase(Locale.getDefault());
            _dict.fillSegment(key.toCharArray());
            return key;
        }
        else
            return null;
    }

    public void loadUserDict(String userDictPath) {
        try {
            File file = new File(userDictPath);
            InputStream fis=new FileInputStream(file);
            BufferedReader br=new BufferedReader(new InputStreamReader(fis));

            long s = System.currentTimeMillis();
            int count = 0;
            while (br.ready()) {
                String line = br.readLine();
                String[] tokens = line.split("[\t ]+");

                if (tokens.length < 1) {
                    // Ignore empty line
                    continue;
                }

                String word = tokens[0];

                double freq = 3.0d;
                if (tokens.length == 2)
                    freq = Double.valueOf(tokens[1]);
                word = addWord(word); 
                freqs.put(word, Math.log(freq / total));
                count++;
            }
            System.out.println(String.format(Locale.getDefault(), "user dict %s load finished, tot words:%d, time elapsed:%dms", userDictPath, count, System.currentTimeMillis() - s));
            br.close();
        }
        catch (IOException e) {
            System.err.println(String.format(Locale.getDefault(), "%s: load user dict failure!", userDictPath));
        }
    }


    public DictSegment getTrie() {
        return this._dict;
    }


    public boolean containsWord(String word) {
        return freqs.containsKey(word);
    }


    public Double getFreq(String key) {
        if (containsWord(key))
            return freqs.get(key);
        else
            return minFreq;
    }
}
