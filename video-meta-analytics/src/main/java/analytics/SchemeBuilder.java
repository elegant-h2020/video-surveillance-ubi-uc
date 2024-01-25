package analytics;

/**
 * @author Ioannis Plakas
 * @email iplakas@ubitech.eu
 * @date 1/31/23
 */

enum BasicDataType {
    INT8,
    INT16,
    INT32,
    INT64,
    UINT8,
    UINT16,
    UINT32,
    UINT64,
    FLOAT32
}

public class SchemeBuilder {
    private String scheme;

    public SchemeBuilder() {
        this.scheme = "Schema::create()";
    }

//    public SchemeBuilder addField(String fieldName, BasicDataType type) {
//        this.scheme += String.format("->addField(\"%s\",%s)", fieldName, type.toString());
//        return this;
//    }

    public SchemeBuilder addField(String fieldName, BasicDataType type) {
        this.scheme += String.format("->addField(\"%s\",BasicType::%s)", fieldName, type.toString());
        return this;
    }
    private String functionSelector(BasicDataType type) {
        switch(type) {
            case INT8:
                return "DataTypeFactory::createInt8()";
            case INT16:
                return "DataTypeFactory::createInt16()";
            case INT32:
                return "DataTypeFactory::createInt32()";
            case INT64:
                return "DataTypeFactory::createInt64()";
            case UINT8:
                return "DataTypeFactory::createUInt8()";
            case UINT16:
                return "DataTypeFactory::createUInt16()";
            case UINT32:
                return "DataTypeFactory::createUInt32()";
            case UINT64:
                return "DataTypeFactory::createUInt64()";
            case FLOAT32:
                return "DataTypeFactory::createFloat32()";
            default:
                return "Unkwnown";
        }
    }

    public SchemeBuilder addArrayField(String fieldName, BasicDataType innerType, int length) {
        this.scheme += String.format("->addField(\"%s\",DataTypeFactory::createArray(%d, %s))", fieldName, length, functionSelector(innerType));
        return this;
    }

    public SchemeBuilder addStringField(String fieldName, int length) {
        this.scheme += String.format("->addField(\"%s\",DataTypeFactory::createFixedChar(%d))", fieldName, length);
        return this;
    }

    public String build() {
        this.scheme += ";";
        return this.scheme;
    }
}
