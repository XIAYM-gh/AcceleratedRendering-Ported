package com.github.argon4w.acceleratedrendering.core.backends.programs;

import lombok.Getter;

import static org.lwjgl.opengl.GL46.*;

@Getter
public class ComputeShader {

    private final int shaderHandle;

    public ComputeShader() {
        this.shaderHandle = glCreateShader(GL_COMPUTE_SHADER);
    }

    public void setShaderSource(String source) {
        glShaderSource(shaderHandle, source);
    }

    public boolean compileShader() {
        glCompileShader(shaderHandle);
        return glGetShaderi(shaderHandle, GL_COMPILE_STATUS) == GL_TRUE;
    }

    public String getInfoLog() {
        return glGetShaderInfoLog(shaderHandle);
    }

    public void delete() {
        glDeleteShader(shaderHandle);
    }
}
