package lv.llu.science.bees.webapi.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class MongoZonedDateTimeConverters {

    public static List<Converter<?, ?>> getConverters() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(DateToZonedDateTimeConverter.INSTANCE);
        converters.add(ZonedDateTimeToDateConverter.INSTANCE);
        return converters;
    }

    @ReadingConverter
    public enum DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {

        INSTANCE;

        @Override
        public ZonedDateTime convert(Date source) {
            return ZonedDateTime.ofInstant(source.toInstant(), ZoneOffset.UTC);
        }
    }

    @WritingConverter
    public enum ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {

        INSTANCE;

        @Override
        public Date convert(ZonedDateTime source) {
            return Date.from(source.toInstant());
        }
    }

}
