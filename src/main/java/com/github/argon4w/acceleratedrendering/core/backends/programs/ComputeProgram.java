package com.github.argon4w.acceleratedrendering.core.backends.programs;

import lombok.Getter;

import static org.lwjgl.opengl.GL46.*;

@Getter
public class ComputeProgram {

    private final int programHandle;
    private final int barrierFlags;

    public ComputeProgram(int barrierFlags) {
        this.programHandle = glCreateProgram();
        this.barrierFlags = barrierFlags;
    }

    public void dispatch(
            int countX,
            int countY,
            int countZ
    ) {
        glDispatchCompute(
                countX,
                countY,
                countZ
        );
    }

    public boolean linkProgram() {
        glLinkProgram(programHandle);
        return glGetProgrami(programHandle, GL_LINK_STATUS) == GL_TRUE;
    }

    public void useProgram() {
        glUseProgram(programHandle);
    }

    public void resetProgram() {
        glUseProgram(0);
    }

    public void attachShader(ComputeShader computeShader) {
        glAttachShader(programHandle, computeShader.getShaderHandle());
    }

    public void waitBarriers() {
        glMemoryBarrier(barrierFlags);
    }

    public int getUniformLocation(String name) {
        return glGetUniformLocation(programHandle, name);
    }

    public Uniform getUniform(String name) {
        return new Uniform(programHandle, getUniformLocation(name));
    }

    public String getInfoLog() {
        return glGetProgramInfoLog(programHandle);
    }

    public void delete() {
        glDeleteProgram(programHandle);
    }
}
