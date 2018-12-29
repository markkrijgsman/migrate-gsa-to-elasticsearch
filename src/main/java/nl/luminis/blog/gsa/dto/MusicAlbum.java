package nl.luminis.blog.gsa.dto;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import lombok.Data;

@Data
public class MusicAlbum {

    public static final String[] SEARCHABLE_FIELDS = Stream.of(MusicAlbum.class.getDeclaredFields())
                                                           .filter(field -> Modifier.isPrivate(field.getModifiers()))
                                                           .map(Field::getName)
                                                           .toArray(String[]::new);

    private Integer id;
    private String album;
    private String artist;
    private String label;
    private Integer year;
    private String information;

}
