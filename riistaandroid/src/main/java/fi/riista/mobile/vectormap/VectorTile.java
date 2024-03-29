// Generated by the protocol buffer compiler.  DO NOT EDIT!

package fi.riista.mobile.vectormap;

@SuppressWarnings("hiding")
public interface VectorTile {

  public static final class Tile extends
      com.google.protobuf.nano.ExtendableMessageNano<Tile> {

    // enum GeomType
    public static final int UNKNOWN = 0;
    public static final int POINT = 1;
    public static final int LINESTRING = 2;
    public static final int POLYGON = 3;

    public static final class Value extends
        com.google.protobuf.nano.ExtendableMessageNano<Value> {

      private static volatile Value[] _emptyArray;
      public static Value[] emptyArray() {
        // Lazily initializes the empty array
        if (_emptyArray == null) {
          synchronized (
              com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
            if (_emptyArray == null) {
              _emptyArray = new Value[0];
            }
          }
        }
        return _emptyArray;
      }

      // optional string string_value = 1;
      public java.lang.String stringValue;

      // optional float float_value = 2;
      public float floatValue;

      // optional double double_value = 3;
      public double doubleValue;

      // optional int64 int_value = 4;
      public long intValue;

      // optional uint64 uint_value = 5;
      public long uintValue;

      // optional sint64 sint_value = 6;
      public long sintValue;

      // optional bool bool_value = 7;
      public boolean boolValue;

      public Value() {
        clear();
      }

      public Value clear() {
        stringValue = "";
        floatValue = 0F;
        doubleValue = 0D;
        intValue = 0L;
        uintValue = 0L;
        sintValue = 0L;
        boolValue = false;
        unknownFieldData = null;
        cachedSize = -1;
        return this;
      }

      @Override
      public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
          throws java.io.IOException {
        if (!this.stringValue.equals("")) {
          output.writeString(1, this.stringValue);
        }
        if (java.lang.Float.floatToIntBits(this.floatValue)
            != java.lang.Float.floatToIntBits(0F)) {
          output.writeFloat(2, this.floatValue);
        }
        if (java.lang.Double.doubleToLongBits(this.doubleValue)
            != java.lang.Double.doubleToLongBits(0D)) {
          output.writeDouble(3, this.doubleValue);
        }
        if (this.intValue != 0L) {
          output.writeInt64(4, this.intValue);
        }
        if (this.uintValue != 0L) {
          output.writeUInt64(5, this.uintValue);
        }
        if (this.sintValue != 0L) {
          output.writeSInt64(6, this.sintValue);
        }
        if (this.boolValue != false) {
          output.writeBool(7, this.boolValue);
        }
        super.writeTo(output);
      }

      @Override
      protected int computeSerializedSize() {
        int size = super.computeSerializedSize();
        if (!this.stringValue.equals("")) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeStringSize(1, this.stringValue);
        }
        if (java.lang.Float.floatToIntBits(this.floatValue)
            != java.lang.Float.floatToIntBits(0F)) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeFloatSize(2, this.floatValue);
        }
        if (java.lang.Double.doubleToLongBits(this.doubleValue)
            != java.lang.Double.doubleToLongBits(0D)) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeDoubleSize(3, this.doubleValue);
        }
        if (this.intValue != 0L) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeInt64Size(4, this.intValue);
        }
        if (this.uintValue != 0L) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeUInt64Size(5, this.uintValue);
        }
        if (this.sintValue != 0L) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeSInt64Size(6, this.sintValue);
        }
        if (this.boolValue != false) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeBoolSize(7, this.boolValue);
        }
        return size;
      }

      @Override
      public Value mergeFrom(
              com.google.protobuf.nano.CodedInputByteBufferNano input)
          throws java.io.IOException {
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              return this;
            default: {
              if (!storeUnknownField(input, tag)) {
                return this;
              }
              break;
            }
            case 10: {
              this.stringValue = input.readString();
              break;
            }
            case 21: {
              this.floatValue = input.readFloat();
              break;
            }
            case 25: {
              this.doubleValue = input.readDouble();
              break;
            }
            case 32: {
              this.intValue = input.readInt64();
              break;
            }
            case 40: {
              this.uintValue = input.readUInt64();
              break;
            }
            case 48: {
              this.sintValue = input.readSInt64();
              break;
            }
            case 56: {
              this.boolValue = input.readBool();
              break;
            }
          }
        }
      }

      public static Value parseFrom(byte[] data)
          throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
        return com.google.protobuf.nano.MessageNano.mergeFrom(new Value(), data);
      }

      public static Value parseFrom(
              com.google.protobuf.nano.CodedInputByteBufferNano input)
          throws java.io.IOException {
        return new Value().mergeFrom(input);
      }
    }

    public static final class Feature extends
        com.google.protobuf.nano.ExtendableMessageNano<Feature> {

      private static volatile Feature[] _emptyArray;
      public static Feature[] emptyArray() {
        // Lazily initializes the empty array
        if (_emptyArray == null) {
          synchronized (
              com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
            if (_emptyArray == null) {
              _emptyArray = new Feature[0];
            }
          }
        }
        return _emptyArray;
      }

      // optional uint64 id = 1 [default = 0];
      public long id;

      // repeated uint32 tags = 2 [packed = true];
      public int[] tags;

      // optional .vector_tile.Tile.GeomType type = 3 [default = UNKNOWN];
      public int type;

      // repeated uint32 geometry = 4 [packed = true];
      public int[] geometry;

      public Feature() {
        clear();
      }

      public Feature clear() {
        id = 0L;
        tags = com.google.protobuf.nano.WireFormatNano.EMPTY_INT_ARRAY;
        type = VectorTile.Tile.UNKNOWN;
        geometry = com.google.protobuf.nano.WireFormatNano.EMPTY_INT_ARRAY;
        unknownFieldData = null;
        cachedSize = -1;
        return this;
      }

      @Override
      public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
          throws java.io.IOException {
        if (this.id != 0L) {
          output.writeUInt64(1, this.id);
        }
        if (this.tags != null && this.tags.length > 0) {
          int dataSize = 0;
          for (int i = 0; i < this.tags.length; i++) {
            int element = this.tags[i];
            dataSize += com.google.protobuf.nano.CodedOutputByteBufferNano
                .computeUInt32SizeNoTag(element);
          }
          output.writeRawVarint32(18);
          output.writeRawVarint32(dataSize);
          for (int i = 0; i < this.tags.length; i++) {
            output.writeUInt32NoTag(this.tags[i]);
          }
        }
        if (this.type != VectorTile.Tile.UNKNOWN) {
          output.writeInt32(3, this.type);
        }
        if (this.geometry != null && this.geometry.length > 0) {
          int dataSize = 0;
          for (int i = 0; i < this.geometry.length; i++) {
            int element = this.geometry[i];
            dataSize += com.google.protobuf.nano.CodedOutputByteBufferNano
                .computeUInt32SizeNoTag(element);
          }
          output.writeRawVarint32(34);
          output.writeRawVarint32(dataSize);
          for (int i = 0; i < this.geometry.length; i++) {
            output.writeUInt32NoTag(this.geometry[i]);
          }
        }
        super.writeTo(output);
      }

      @Override
      protected int computeSerializedSize() {
        int size = super.computeSerializedSize();
        if (this.id != 0L) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeUInt64Size(1, this.id);
        }
        if (this.tags != null && this.tags.length > 0) {
          int dataSize = 0;
          for (int i = 0; i < this.tags.length; i++) {
            int element = this.tags[i];
            dataSize += com.google.protobuf.nano.CodedOutputByteBufferNano
                .computeUInt32SizeNoTag(element);
          }
          size += dataSize;
          size += 1;
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeRawVarint32Size(dataSize);
        }
        if (this.type != VectorTile.Tile.UNKNOWN) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeInt32Size(3, this.type);
        }
        if (this.geometry != null && this.geometry.length > 0) {
          int dataSize = 0;
          for (int i = 0; i < this.geometry.length; i++) {
            int element = this.geometry[i];
            dataSize += com.google.protobuf.nano.CodedOutputByteBufferNano
                .computeUInt32SizeNoTag(element);
          }
          size += dataSize;
          size += 1;
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeRawVarint32Size(dataSize);
        }
        return size;
      }

      @Override
      public Feature mergeFrom(
              com.google.protobuf.nano.CodedInputByteBufferNano input)
          throws java.io.IOException {
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              return this;
            default: {
              if (!storeUnknownField(input, tag)) {
                return this;
              }
              break;
            }
            case 8: {
              this.id = input.readUInt64();
              break;
            }
            case 16: {
              int arrayLength = com.google.protobuf.nano.WireFormatNano
                  .getRepeatedFieldArrayLength(input, 16);
              int i = this.tags == null ? 0 : this.tags.length;
              int[] newArray = new int[i + arrayLength];
              if (i != 0) {
                java.lang.System.arraycopy(this.tags, 0, newArray, 0, i);
              }
              for (; i < newArray.length - 1; i++) {
                newArray[i] = input.readUInt32();
                input.readTag();
              }
              // Last one without readTag.
              newArray[i] = input.readUInt32();
              this.tags = newArray;
              break;
            }
            case 18: {
              int length = input.readRawVarint32();
              int limit = input.pushLimit(length);
              // First pass to compute array length.
              int arrayLength = 0;
              int startPos = input.getPosition();
              while (input.getBytesUntilLimit() > 0) {
                input.readUInt32();
                arrayLength++;
              }
              input.rewindToPosition(startPos);
              int i = this.tags == null ? 0 : this.tags.length;
              int[] newArray = new int[i + arrayLength];
              if (i != 0) {
                java.lang.System.arraycopy(this.tags, 0, newArray, 0, i);
              }
              for (; i < newArray.length; i++) {
                newArray[i] = input.readUInt32();
              }
              this.tags = newArray;
              input.popLimit(limit);
              break;
            }
            case 24: {
              int value = input.readInt32();
              switch (value) {
                case VectorTile.Tile.UNKNOWN:
                case VectorTile.Tile.POINT:
                case VectorTile.Tile.LINESTRING:
                case VectorTile.Tile.POLYGON:
                  this.type = value;
                  break;
              }
              break;
            }
            case 32: {
              int arrayLength = com.google.protobuf.nano.WireFormatNano
                  .getRepeatedFieldArrayLength(input, 32);
              int i = this.geometry == null ? 0 : this.geometry.length;
              int[] newArray = new int[i + arrayLength];
              if (i != 0) {
                java.lang.System.arraycopy(this.geometry, 0, newArray, 0, i);
              }
              for (; i < newArray.length - 1; i++) {
                newArray[i] = input.readUInt32();
                input.readTag();
              }
              // Last one without readTag.
              newArray[i] = input.readUInt32();
              this.geometry = newArray;
              break;
            }
            case 34: {
              int length = input.readRawVarint32();
              int limit = input.pushLimit(length);
              // First pass to compute array length.
              int arrayLength = 0;
              int startPos = input.getPosition();
              while (input.getBytesUntilLimit() > 0) {
                input.readUInt32();
                arrayLength++;
              }
              input.rewindToPosition(startPos);
              int i = this.geometry == null ? 0 : this.geometry.length;
              int[] newArray = new int[i + arrayLength];
              if (i != 0) {
                java.lang.System.arraycopy(this.geometry, 0, newArray, 0, i);
              }
              for (; i < newArray.length; i++) {
                newArray[i] = input.readUInt32();
              }
              this.geometry = newArray;
              input.popLimit(limit);
              break;
            }
          }
        }
      }

      public static Feature parseFrom(byte[] data)
          throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
        return com.google.protobuf.nano.MessageNano.mergeFrom(new Feature(), data);
      }

      public static Feature parseFrom(
              com.google.protobuf.nano.CodedInputByteBufferNano input)
          throws java.io.IOException {
        return new Feature().mergeFrom(input);
      }
    }

    public static final class Layer extends
        com.google.protobuf.nano.ExtendableMessageNano<Layer> {

      private static volatile Layer[] _emptyArray;
      public static Layer[] emptyArray() {
        // Lazily initializes the empty array
        if (_emptyArray == null) {
          synchronized (
              com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
            if (_emptyArray == null) {
              _emptyArray = new Layer[0];
            }
          }
        }
        return _emptyArray;
      }

      // required uint32 version = 15 [default = 1];
      public int version;

      // required string name = 1;
      public java.lang.String name;

      // repeated .vector_tile.Tile.Feature features = 2;
      public VectorTile.Tile.Feature[] features;

      // repeated string keys = 3;
      public java.lang.String[] keys;

      // repeated .vector_tile.Tile.Value values = 4;
      public VectorTile.Tile.Value[] values;

      // optional uint32 extent = 5 [default = 4096];
      public int extent;

      public Layer() {
        clear();
      }

      public Layer clear() {
        version = 1;
        name = "";
        features = VectorTile.Tile.Feature.emptyArray();
        keys = com.google.protobuf.nano.WireFormatNano.EMPTY_STRING_ARRAY;
        values = VectorTile.Tile.Value.emptyArray();
        extent = 4096;
        unknownFieldData = null;
        cachedSize = -1;
        return this;
      }

      @Override
      public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
          throws java.io.IOException {
        output.writeString(1, this.name);
        if (this.features != null && this.features.length > 0) {
          for (int i = 0; i < this.features.length; i++) {
            VectorTile.Tile.Feature element = this.features[i];
            if (element != null) {
              output.writeMessage(2, element);
            }
          }
        }
        if (this.keys != null && this.keys.length > 0) {
          for (int i = 0; i < this.keys.length; i++) {
            java.lang.String element = this.keys[i];
            if (element != null) {
              output.writeString(3, element);
            }
          }
        }
        if (this.values != null && this.values.length > 0) {
          for (int i = 0; i < this.values.length; i++) {
            VectorTile.Tile.Value element = this.values[i];
            if (element != null) {
              output.writeMessage(4, element);
            }
          }
        }
        if (this.extent != 4096) {
          output.writeUInt32(5, this.extent);
        }
        output.writeUInt32(15, this.version);
        super.writeTo(output);
      }

      @Override
      protected int computeSerializedSize() {
        int size = super.computeSerializedSize();
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeStringSize(1, this.name);
        if (this.features != null && this.features.length > 0) {
          for (int i = 0; i < this.features.length; i++) {
            VectorTile.Tile.Feature element = this.features[i];
            if (element != null) {
              size += com.google.protobuf.nano.CodedOutputByteBufferNano
                .computeMessageSize(2, element);
            }
          }
        }
        if (this.keys != null && this.keys.length > 0) {
          int dataCount = 0;
          int dataSize = 0;
          for (int i = 0; i < this.keys.length; i++) {
            java.lang.String element = this.keys[i];
            if (element != null) {
              dataCount++;
              dataSize += com.google.protobuf.nano.CodedOutputByteBufferNano
                  .computeStringSizeNoTag(element);
            }
          }
          size += dataSize;
          size += 1 * dataCount;
        }
        if (this.values != null && this.values.length > 0) {
          for (int i = 0; i < this.values.length; i++) {
            VectorTile.Tile.Value element = this.values[i];
            if (element != null) {
              size += com.google.protobuf.nano.CodedOutputByteBufferNano
                .computeMessageSize(4, element);
            }
          }
        }
        if (this.extent != 4096) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeUInt32Size(5, this.extent);
        }
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeUInt32Size(15, this.version);
        return size;
      }

      @Override
      public Layer mergeFrom(
              com.google.protobuf.nano.CodedInputByteBufferNano input)
          throws java.io.IOException {
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              return this;
            default: {
              if (!storeUnknownField(input, tag)) {
                return this;
              }
              break;
            }
            case 10: {
              this.name = input.readString();
              break;
            }
            case 18: {
              int arrayLength = com.google.protobuf.nano.WireFormatNano
                  .getRepeatedFieldArrayLength(input, 18);
              int i = this.features == null ? 0 : this.features.length;
              VectorTile.Tile.Feature[] newArray =
                  new VectorTile.Tile.Feature[i + arrayLength];
              if (i != 0) {
                java.lang.System.arraycopy(this.features, 0, newArray, 0, i);
              }
              for (; i < newArray.length - 1; i++) {
                newArray[i] = new VectorTile.Tile.Feature();
                input.readMessage(newArray[i]);
                input.readTag();
              }
              // Last one without readTag.
              newArray[i] = new VectorTile.Tile.Feature();
              input.readMessage(newArray[i]);
              this.features = newArray;
              break;
            }
            case 26: {
              int arrayLength = com.google.protobuf.nano.WireFormatNano
                  .getRepeatedFieldArrayLength(input, 26);
              int i = this.keys == null ? 0 : this.keys.length;
              java.lang.String[] newArray = new java.lang.String[i + arrayLength];
              if (i != 0) {
                java.lang.System.arraycopy(this.keys, 0, newArray, 0, i);
              }
              for (; i < newArray.length - 1; i++) {
                newArray[i] = input.readString();
                input.readTag();
              }
              // Last one without readTag.
              newArray[i] = input.readString();
              this.keys = newArray;
              break;
            }
            case 34: {
              int arrayLength = com.google.protobuf.nano.WireFormatNano
                  .getRepeatedFieldArrayLength(input, 34);
              int i = this.values == null ? 0 : this.values.length;
              VectorTile.Tile.Value[] newArray =
                  new VectorTile.Tile.Value[i + arrayLength];
              if (i != 0) {
                java.lang.System.arraycopy(this.values, 0, newArray, 0, i);
              }
              for (; i < newArray.length - 1; i++) {
                newArray[i] = new VectorTile.Tile.Value();
                input.readMessage(newArray[i]);
                input.readTag();
              }
              // Last one without readTag.
              newArray[i] = new VectorTile.Tile.Value();
              input.readMessage(newArray[i]);
              this.values = newArray;
              break;
            }
            case 40: {
              this.extent = input.readUInt32();
              break;
            }
            case 120: {
              this.version = input.readUInt32();
              break;
            }
          }
        }
      }

      public static Layer parseFrom(byte[] data)
          throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
        return com.google.protobuf.nano.MessageNano.mergeFrom(new Layer(), data);
      }

      public static Layer parseFrom(
              com.google.protobuf.nano.CodedInputByteBufferNano input)
          throws java.io.IOException {
        return new Layer().mergeFrom(input);
      }
    }

    private static volatile Tile[] _emptyArray;
    public static Tile[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new Tile[0];
          }
        }
      }
      return _emptyArray;
    }

    // repeated .vector_tile.Tile.Layer layers = 3;
    public VectorTile.Tile.Layer[] layers;

    public Tile() {
      clear();
    }

    public Tile clear() {
      layers = VectorTile.Tile.Layer.emptyArray();
      unknownFieldData = null;
      cachedSize = -1;
      return this;
    }

    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.layers != null && this.layers.length > 0) {
        for (int i = 0; i < this.layers.length; i++) {
          VectorTile.Tile.Layer element = this.layers[i];
          if (element != null) {
            output.writeMessage(3, element);
          }
        }
      }
      super.writeTo(output);
    }

    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.layers != null && this.layers.length > 0) {
        for (int i = 0; i < this.layers.length; i++) {
          VectorTile.Tile.Layer element = this.layers[i];
          if (element != null) {
            size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeMessageSize(3, element);
          }
        }
      }
      return size;
    }

    @Override
    public Tile mergeFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      while (true) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            return this;
          default: {
            if (!storeUnknownField(input, tag)) {
              return this;
            }
            break;
          }
          case 26: {
            int arrayLength = com.google.protobuf.nano.WireFormatNano
                .getRepeatedFieldArrayLength(input, 26);
            int i = this.layers == null ? 0 : this.layers.length;
            VectorTile.Tile.Layer[] newArray =
                new VectorTile.Tile.Layer[i + arrayLength];
            if (i != 0) {
              java.lang.System.arraycopy(this.layers, 0, newArray, 0, i);
            }
            for (; i < newArray.length - 1; i++) {
              newArray[i] = new VectorTile.Tile.Layer();
              input.readMessage(newArray[i]);
              input.readTag();
            }
            // Last one without readTag.
            newArray[i] = new VectorTile.Tile.Layer();
            input.readMessage(newArray[i]);
            this.layers = newArray;
            break;
          }
        }
      }
    }

    public static Tile parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new Tile(), data);
    }

    public static Tile parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new Tile().mergeFrom(input);
    }
  }
}
