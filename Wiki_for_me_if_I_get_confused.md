# Nodes

Задаёт необходимый тип данных

    @Override public boolean acceptsInputType(int port, ValueType type) { return type == ValueType.SOMETHING; }
Задаёт определённый порт быть нужным типом данных
```
    @Override
    public ValueType getInputType(int port) {
        if (port == 0) {
            return ValueType.LIST;
        }
        if (port == 1 || port == 2) {
            return ValueType.INT;
        }
        return ValueType.NULL;
    }
```
Нужно определить ещё выходные порты
```
    @Override
    public ValueType getOutputType(int port) {
        return switch (port) {
            case 0 -> ValueType.BOOLEAN; // hit
            case 1 -> ValueType.FLOAT;   // distance
            case 2 -> ValueType.STRING;  // blockId
            case 3 -> ValueType.BOOLEAN; // isShip
            default -> ValueType.NULL;
        };
    }

```



## Отправка пакета с устройства на сервер
Из ServerBlockEntity

    public void receivePacket(AnalogDevicesPacket packet)
Пишется if который отвечает какой сигнал приходит и что с ним делать

