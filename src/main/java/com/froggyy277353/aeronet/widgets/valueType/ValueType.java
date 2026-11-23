package com.froggyy277353.aeronet.widgets.valueType;

public enum ValueType {
    INT,
    FLOAT,
    BOOLEAN,
    STRING,
    NULL, LIST;

    public boolean canConvertTo(ValueType target) {
        if (this == NULL || target == NULL) {
            return false;
        }
        return this == target || (isNumericOrBoolean() && target.isNumericOrBoolean());
    }

    private boolean isNumericOrBoolean() {
        return this == INT || this == FLOAT || this == BOOLEAN;
    }
}
