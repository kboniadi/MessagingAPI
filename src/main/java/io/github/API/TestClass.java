package io.github.API;

import lombok.Getter;

@Getter
class TestClass {
    private final String name;
    private final String haircolor;
    private final String[] utsav;

    public TestClass(String name, String haircolor, String[] utsav) {
        this.name = name;
        this.haircolor = haircolor;
        this.utsav = utsav;
    }
}
