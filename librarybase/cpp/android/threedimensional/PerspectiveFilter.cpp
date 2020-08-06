//
// Created by rayyy on 2020/8/5.
//

#include "PerspectiveFilter.h"
#include "BaseGLUtils.h"

namespace threedimensional {

// 顶点着色器
const static char* DILATE_FILTER_SHADER_VERTEX = ""
    "attribute vec4 position;\n"
    "attribute vec4 inputTextureCoordinate;\n"
    "uniform mat4 mvpMatrix;\n"
    "varying vec2 textureCoordinate;\n"
    "void main()\n"
    "{\n"
    "    gl_Position = mvpMatrix * position;\n"
    "    textureCoordinate = inputTextureCoordinate.xy;\n"
    "}\n";

// 片元着色器
const static char* DILATE_FILTER_SHADER_FRAGMENT = ""
    "precision highp float;\n"
    "varying vec2 textureCoordinate;\n"
    "uniform sampler2D inputImageTexture;\n"
    "void main()\n"
    "{\n"
    "    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"
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

void PerspectiveFilter::init(int width, int height) {
  program = BaseGLUtils::createProgram(DILATE_FILTER_SHADER_VERTEX, DILATE_FILTER_SHADER_FRAGMENT);
  if (program > 0) {
    // 获取顶点坐标位置
    positionAttribute = glGetAttribLocation(program, "position");
    // 获取纹理坐标位置
    textureCoordinateAttribute =
        glGetAttribLocation(program, "inputTextureCoordinate");
    // 获取纹理采样器位置
    inputImageTextureUniform = glGetUniformLocation(program, "inputImageTexture");
    mvpMatrixUniform = glGetUniformLocation(program, "mvpMatrix");
  }

  this->width = width;
  this->height = height;
  outputTextureID = BaseGLUtils::createTexture2D();
  outputFrameBufferID = BaseGLUtils::createFBO(outputTextureID, width, height);
}

void PerspectiveFilter::release() {
  glDeleteProgram(program);
  program = 0;
  glDeleteTextures(1, &outputTextureID);
  outputTextureID = 0;
  glDeleteFramebuffers(1, &outputFrameBufferID);
  outputFrameBufferID = 0;
  width = 0;
  height = 0;
}

GLuint PerspectiveFilter::render(GLuint inputTextureID, GLfloat mvpMatrix[16]) {
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

  // 传入顶点位置
  glEnableVertexAttribArray(positionAttribute);
  glVertexAttribPointer(positionAttribute, 2, GL_FLOAT, false, 8,
      imageVertices);

  // 传入纹理位置
  glEnableVertexAttribArray(textureCoordinateAttribute);
  glVertexAttribPointer(textureCoordinateAttribute, 2, GL_FLOAT, false, 8,
      textureCoordinates);

  glUniformMatrix4fv(mvpMatrixUniform, 1, false, mvpMatrix);

  // 绘制
  glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

  glDisableVertexAttribArray(positionAttribute);
  glDisableVertexAttribArray(textureCoordinateAttribute);

  glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE);

  return outputTextureID;
}

}