package lv.llu.science.bees.webapi.utils.validators

import spock.lang.Specification

class ElementsNotNullValidatorSpec extends Specification {

    def validator = new ElementsNotNullValidator()

    def "should validate"() {
        expect:
            validator.isValid(list, null) == result
        where:
            list         | result
            [null, null] | false
            [1, null, 3] | false
            [1, 2, 3]    | true
            null         | false
    }
}
