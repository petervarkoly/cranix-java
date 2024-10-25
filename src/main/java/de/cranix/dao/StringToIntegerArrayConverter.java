package de.cranix.dao;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class StringToIntegerArrayConverter implements AttributeConverter<List<Integer>, String> {
    private static final String SPLIT_CHAR = ";";

    @Override
    public String convertToDatabaseColumn(List<Integer> intList ) {
        List<String> stringList = new ArrayList<>() ;
        if( intList != null ) {
            for(Integer i: intList){
                stringList.add(toString());
            }
        }
        return String.join(SPLIT_CHAR, stringList);
    }

    @Override
    public List<Integer> convertToEntityAttribute(String string) {
        List<Integer> integerList = new ArrayList<>();
        for(String s: string.split(SPLIT_CHAR)){
            integerList.add(Integer.getInteger(s));
        }
        return integerList;
    }
}
