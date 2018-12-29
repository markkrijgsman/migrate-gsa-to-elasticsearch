package nl.luminis.blog.gsa.dsl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BooleanConverterTest {

    @InjectMocks
    private BooleanConverter converter;

    @Test
    public void testMapValue() {
        assertThat(converter.convert("1")).isEqualTo("true");
        assertThat(converter.convert("0")).isEqualTo("false");
        assertThat(converter.convert("x")).isEqualTo("x");
    }
}
