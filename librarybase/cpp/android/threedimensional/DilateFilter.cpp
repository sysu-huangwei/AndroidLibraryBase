//
// Created by rayyy on 2020/8/5.
//

#include "DilateFilter.h"
#include "BaseGLUtils.h"

namespace threedimensional {

// 顶点着色器
const static char* DILATE_FILTER_SHADER_VERTEX = ""
    "attribute vec4 position;\n"
    "attribute vec4 inputTextureCoordinate;\n"
    "varying vec2 textureCoordinate;\n"
    "varying vec2 textureCoordinateOneStepPositive;\n"
    "varying vec2 textureCoordinateOneStepNegative;\n"
    "uniform float texelWidthOffset;\n"
    "uniform float texelHeightOffset;\n"
    "void main()\n"
    "{\n"
    "    vec2 offset = vec2(texelWidthOffset, texelHeightOffset);\n"
    "    gl_Position = position;\n"
    "    textureCoordinate = inputTextureCoordinate.xy;\n"
    "    textureCoordinateOneStepPositive = inputTextureCoordinate.xy + offset;\n"
    "    textureCoordinateOneStepNegative = inputTextureCoordinate.xy - offset;\n"
    "}\n";

// 片元着色器
const static char* DILATE_FILTER_SHADER_FRAGMENT = ""
    "precision highp float;\n"
    "varying vec2 textureCoordinate;\n"
    "varying vec2 textureCoordinateOneStepPositive;\n"
    "varying vec2 textureCoordinateOneStepNegative;\n"
    "uniform sampler2D inputImageTexture;\n"
    "void main()\n"
    "{\n"
    "    vec4 centerColor = texture2D(inputImageTexture, textureCoordinate);\n"
    "    vec4 oneStepPositiveColor = texture2D(inputImageTexture, textureCoordinateOneStepPositive);\n"
    "    vec4 oneStepNegativeColor = texture2D(inputImageTexture, textureCoordinateOneStepNegative);\n"
    "    float maxR = max(centerColor.r, oneStepPositiveColor.r);\n"
    "    maxR = max(maxR, oneStepNegativeColor.r);\n"
    "    float maxG = max(centerColor.g, oneStepPositiveColor.g);\n"
    "    maxG = max(maxG, oneStepNegativeColor.g);\n"
    "    float maxB = max(centerColor.b, oneStepPositiveColor.b);\n"
    "    maxB = max(maxB, oneStepNegativeColor.b);\n"
    "    gl_FragColor = vec4(maxR, maxG, maxB, centerColor.a);\n"
    "}\n";

// 顶点坐标
const static GLfloat imageVertices[8] = {
    -1.0f, 1.0f, // top left
    1.0f, 1.0f,  // top right
    -1.0f, -1.0f, // bottom left
    1.0f, -1.0f, // bottom right
};

// 纹理坐标
const static GLfloat textureCoordinates[8] = {
    0.0f, 1.0f, // bottom left
    1.0f, 1.0f, // bottom right
    0.0f, 0.0f, // top left
    1.0f, 0.0f, // top right
};

void DilateFilter::init(int width, int height) {
  program = BaseGLUtils::createProgram(DILATE_FILTER_SHADER_VERTEX, DILATE_FILTER_SHADER_FRAGMENT);
  if (program > 0) {
    // 获取顶点坐标位置
    positionAttribute = glGetAttribLocation(program, "position");
    // 获取纹理坐标位置
    textureCoordinateAttribute =
        glGetAttribLocation(program, "inputTextureCoordinate");
    // 获取纹理采样器位置
    inputImageTextureUniform = glGetUniformLocation(program, "inputImageTexture");
    texelWidthOffsetUniform = glGetUniformLocation(program, "texelWidthOffset");
    texelHeightOffsetUniform = glGetUniformLocation(program, "texelHeightOffset");
  }

  this->width = width;
  this->height = height;
  outputTextureID = BaseGLUtils::createTexture2D();
  outputFrameBufferID = BaseGLUtils::createFBO(outputTextureID, width, height);
}

void DilateFilter::release() {
  glDeleteProgram(program);
  program = 0;
  glDeleteTextures(1, &outputTextureID);
  outputTextureID = 0;
  glDeleteFramebuffers(1, &outputFrameBufferID);
  outputFrameBufferID = 0;
  width = 0;
  height = 0;
}

GLuint DilateFilter::render(GLuint inputTextureID) {
  glBindFramebuffer(GL_FRAMEBUFFER, outputFrameBufferID);
  glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
      GL_TEXTURE_2D, outputTextureID, 0);

  glViewport(0, 0, width, height);

  // 清屏
  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

  // 使用着色器绘制程序
  glUseProgram(program);

  // 传入纹理
  glActiveTexture(GL_TEXTURE0);
  glBindTexture(GL_TEXTURE_2D, inputTextureID);
  glUniform1i(inputImageTextureUniform, 0);

  //传入其他参数
  glUniform1f(texelWidthOffsetUniform, 0.02f);
  glUniform1f(texelHeightOffsetUniform, 0.02f);

  // 传入顶点位置
  glEnableVertexAttribArray(positionAttribute);
  glVertexAttribPointer(positionAttribute, 2, GL_FLOAT, false, 8,
      imageVertices);

  // 传入纹理位置
  glEnableVertexAttribArray(textureCoordinateAttribute);
  glVertexAttribPointer(textureCoordinateAttribute, 2, GL_FLOAT, false, 8,
      textureCoordinates);

  // 绘制
  glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

  glDisableVertexAttribArray(positionAttribute);
  glDisableVertexAttribArray(textureCoordinateAttribute);

  glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE);

  return outputTextureID;
}


}
