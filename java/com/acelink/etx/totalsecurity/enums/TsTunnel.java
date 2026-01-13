package com.acelink.etx.totalsecurity.enums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author gregho
 * @since 2018/10/11
 */
public class TsTunnel {

  /*--------------------------------
   * Rule
   *-------------------------------*/
  public enum Rule {

    AUTO(0),
    RELAY_ONLY(1);

    private final int value;

    Rule(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    @Nullable public static Rule fromValue(int value) {
      for (Rule rule : values()) {
        if (rule.value == value) {
          return rule;
        }
      }

      return null;
    }
  }
  /*--------------------------------
   * Type
   *-------------------------------*/
  public enum Type {

    NONE(0),
    P2P(1),
    RELAY(2);

    private final int value;

    Type(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    @NonNull public static Type fromValue(int value) {
      for (Type type : values()) {
        if (type.value == value) {
          return type;
        }
      }

      return NONE;
    }
  }
  /*--------------------------------
   * State
   *-------------------------------*/
  public enum State {

    CLOSE(0),
    OPENING(1),
    OPENED(2),
    CLOSING(3);

    private final int value;

    State(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    @NonNull public static State fromValue(int value) {
      for (State state : values()) {
        if (state.value == value) {
          return state;
        }
      }

      return CLOSE;
    }
  }
}
