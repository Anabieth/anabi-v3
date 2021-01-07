package lv.llu.science.bees.webapi.utils;

import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ModelMapperEx extends ModelMapper {
	public ModelMapperEx() {
		super();
		this.addConverter(new AbstractConverter<LocalDateTime, ZonedDateTime>() {
			@Override
			protected ZonedDateTime convert(LocalDateTime source) {
				return source == null ? null : source.atZone(ZoneId.systemDefault());
			}
		});
	}

	public <T> List<T> mapList(List<?> input, Class<T> targetType) {
		return input.stream().map(element -> this.map(element, targetType)).collect(toList());
	}
}
