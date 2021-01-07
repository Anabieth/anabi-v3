package lv.llu.science.bees.webapi.utils.validators

import spock.lang.Specification

class AnyNotNullValidatorSpec extends Specification {

    static class Bean {
        def val1
        def val2
    }

    AnyNotNullValidator validator

    def setup() {
        validator = new AnyNotNullValidator()

        def ann = Mock(AnyNotNull) {
            fields() >> ['val1', 'val2']
        }

        validator.initialize(ann)
    }


    def "should validate"() {
        expect:
            validator.isValid(object, null) == result
        where:
            object                           | result
            new Bean(val1: 1, val2: 2)       | true
            new Bean(val1: null, val2: 2)    | true
            new Bean(val1: 1, val2: null)    | true
            new Bean(val1: null, val2: null) | false
    }
}
