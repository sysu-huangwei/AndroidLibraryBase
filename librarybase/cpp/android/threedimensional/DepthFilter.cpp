//
// Created by rayyy on 2020/8/5.
//

#include "DepthFilter.h"
#include "BaseGLUtils.h"

namespace threedimensional {

// 顶点着色器
const static char* DEPTH_FILTER_SHADER_VERTEX = ""
    "attribute vec4 position;\n"
    "attribute vec4 inputTextureCoordinate;\n"
    "varying vec2 textureCoordinate;\n"
    "const int MATERIAL_COUNT = 5;\n"
    "varying vec2 materialCoordinateShift[MATERIAL_COUNT];\n"
    "varying float materialDepth[MATERIAL_COUNT];\n"
    "varying vec2 xyOffsetVarying;\n"
    "varying float scaleVarying;\n"
    "varying float focusVarying;\n"
    "uniform vec2 xyOffset;\n"
    "uniform float scale;\n"
    "uniform float focus;\n"
    "uniform float materialDepth0;\n"
    "uniform float materialDepth1;\n"
    "uniform float materialDepth2;\n"
    "uniform float materialDepth3;\n"
    "uniform float materialDepth4;\n"
    "void main()\n"
    "{\n"
    "    gl_Position = position;\n"
    "    textureCoordinate = inputTextureCoordinate.xy;\n"
    "    //把素材图景深值映射到 -0.5 ~ 0.5 (把原点放到focus上)\n"
    "    materialCoordinateShift[0] = textureCoordinate "
    "        + xyOffset * (materialDepth0 - focus) * scale;\n"
    "    materialCoordinateShift[1] = textureCoordinate "
    "        + xyOffset * (materialDepth1 - focus) * scale;\n"
    "    materialCoordinateShift[2] = textureCoordinate "
    "        + xyOffset * (materialDepth2 - focus) * scale;\n"
    "    materialCoordinateShift[3] = textureCoordinate "
    "        + xyOffset * (materialDepth3 - focus) * scale;\n"
    "    materialCoordinateShift[4] = textureCoordinate "
    "        + xyOffset * (materialDepth4 - focus) * scale;\n"
    "    materialDepth[0] = materialDepth0;\n"
    "    materialDepth[1] = materialDepth1;\n"
    "    materialDepth[2] = materialDepth2;\n"
    "    materialDepth[3] = materialDepth3;\n"
    "    materialDepth[4] = materialDepth4;\n"
    "    xyOffsetVarying = xyOffset;\n"
    "    scaleVarying = scale;\n"
    "    focusVarying = focus;\n"
    "}\n";

// 片元着色器
const static char* DEPTH_FILTER_SHADER_FRAGMENT = ""
    "precision highp float;\n"
    "varying vec2 textureCoordinate;\n"
    "const int MATERIAL_COUNT = 5;\n"
    "varying vec2 materialCoordinateShift[MATERIAL_COUNT];\n"
    "varying float materialDepth[MATERIAL_COUNT];\n"
    "varying vec2 xyOffsetVarying;\n"
    "varying float scaleVarying;\n"
    "varying float focusVarying;\n"
    "uniform sampler2D inputImageTexture;\n"
    "uniform sampler2D depthImageTextureProcessed;\n"
    "uniform sampler2D depthImageTextureOrigin;\n"
    "uniform sampler2D materialImageTexture0;\n"
    "uniform sampler2D materialImageTexture1;\n"
    "uniform sampler2D materialImageTexture2;\n"
    "uniform sampler2D materialImageTexture3;\n"
    "uniform sampler2D materialImageTexture4;\n"
    "void main()\n"
    "{\n"
    "    //根据膨胀和模糊之后的景深图计算偏移后的坐标\n"
    "    float depthProcessedAlpha = "
    "        texture2D(depthImageTextureProcessed, textureCoordinate).r;\n"
    "    //把 0 ~ 1 映射到 -0.5 ~ 0.5 (把原点放到focus上)\n"
    "    float mapProcessed = depthProcessedAlpha - focusVarying;\n"
    "    vec2 TexCoordinateShift = textureCoordinate "
    "        + xyOffsetVarying * mapProcessed * scaleVarying;\n"
    "\n"
    "    //把原始的景深图也做一样的偏移效果处理，用于素材的遮盖\n"
    "    vec4 depthOriginColorShift = "
    "        texture2D(depthImageTextureOrigin, TexCoordinateShift);\n"
    "    float depthOriginShiftAlpha = depthOriginColorShift.r;\n"
    "\n"
    "    //原图的颜色\n"
    "    vec4 imageColor = texture2D(inputImageTexture, TexCoordinateShift);\n"
    "    //当前最新的颜色\n"
    "    vec4 currentColor = imageColor;\n"
    "\n"
    "    //计算素材图偏移之后的坐标，根据自身素材的深度进行偏移\n"
    "    if (depthOriginShiftAlpha <= materialDepth[0]) {\n"
    "        vec4 materialColorShift = texture2D(materialImageTexture0, "
    "            materialCoordinateShift[0]);//得到偏移后的素材图\n"
    "        currentColor = mix(currentColor, materialColorShift, "
    "            materialColorShift.a);//素材和原图做融合\n"
    "    }\n"
    "    if (depthOriginShiftAlpha <= materialDepth[1]) {\n"
    "        vec4 materialColorShift = texture2D(materialImageTexture1, "
    "            materialCoordinateShift[1]);//得到偏移后的素材图\n"
    "        currentColor = mix(currentColor, materialColorShift, "
    "            materialColorShift.a);//素材和原图做融合\n"
    "    }\n"
    "    if (depthOriginShiftAlpha <= materialDepth[2]) {\n"
    "        vec4 materialColorShift = texture2D(materialImageTexture2, "
    "            materialCoordinateShift[2]);//得到偏移后的素材图\n"
    "        currentColor = mix(currentColor, materialColorShift, "
    "            materialColorShift.a);//素材和原图做融合\n"
    "    }\n"
    "    if (depthOriginShiftAlpha <= materialDepth[3]) {\n"
    "        vec4 materialColorShift = texture2D(materialImageTexture3, "
    "            materialCoordinateShift[3]);//得到偏移后的素材图\n"
    "        currentColor = mix(currentColor, materialColorShift, "
    "            materialColorShift.a);//素材和原图做融合\n"
    "    }\n"
    "    if (depthOriginShiftAlpha <= materialDepth[4]) {\n"
    "        vec4 materialColorShift = texture2D(materialImageTexture4, "
    "            materialCoordinateShift[4]);//得到偏移后的素材图\n"
    "        currentColor = mix(currentColor, materialColorShift, "
    "            materialColorShift.a);//素材和原图做融合\n"
    "    }\n"
    "\n"
    "    gl_FragColor = currentColor;\n"
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

/* 素材图景深值的最大值，越大表示越靠近屏幕 */
/* 外部会传递 0 ~ 1 的景深值，在内部映射到 0 ~ MATERIAL_DEPTH_MAX 上 */
const static float MATERIAL_DEPTH_MAX = 2.0f;

void DepthFilter::init(int width, int height) {
  program = BaseGLUtils::createProgram(DEPTH_FILTER_SHADER_VERTEX, DEPTH_FILTER_SHADER_FRAGMENT);
  if (program > 0) {
    // 获取顶点坐标位置
    positionAttribute = glGetAttribLocation(program, "position");
    // 获取纹理坐标位置
    textureCoordinateAttribute = 
        glGetAttribLocation(program, "inputTextureCoordinate");
    // 获取纹理采样器位置
    inputImageTextureUniform = glGetUniformLocation(program, "inputImageTexture");
    depthImageTextureProcessedUniform = 
        glGetUniformLocation(program, "depthImageTextureProcessed");
    depthImageTextureOriginUniform = 
        glGetUniformLocation(program, "depthImageTextureOrigin");
    materialImageTextureUniform[0] = 
        glGetUniformLocation(program, "materialImageTexture0");
    materialDepthUniform[0] = glGetUniformLocation(program, "materialDepth0");
    materialImageTextureUniform[1] = 
        glGetUniformLocation(program, "materialImageTexture1");
    materialDepthUniform[1] = glGetUniformLocation(program, "materialDepth1");
    materialImageTextureUniform[2] = 
        glGetUniformLocation(program, "materialImageTexture2");
    materialDepthUniform[2] = glGetUniformLocation(program, "materialDepth2");
    materialImageTextureUniform[3] = 
        glGetUniformLocation(program, "materialImageTexture3");
    materialDepthUniform[3] = glGetUniformLocation(program, "materialDepth3");
    materialImageTextureUniform[4] = 
        glGetUniformLocation(program, "materialImageTexture4");
    materialDepthUniform[4] = glGetUniformLocation(program, "materialDepth4");
    xyOffsetUniform = glGetUniformLocation(program, "xyOffset");
    scaleUniform = glGetUniformLocation(program, "scale");
    focusUniform = glGetUniformLocation(program, "focus");
  }

  this->width = width;
  this->height = height;
  outputTextureID = BaseGLUtils::createTexture2D();
  outputFrameBufferID = BaseGLUtils::createFBO(outputTextureID, width, height);
}

void DepthFilter::release() {
    glDeleteProgram(program);
    program = 0;
    glDeleteTextures(1, &outputTextureID);
    outputTextureID = 0;
    glDeleteFramebuffers(1, &outputFrameBufferID);
    outputFrameBufferID = 0;
    width = 0;
    height = 0;
}
GLuint DepthFilter::render(GLuint inputTextureID,
                           GLuint depthTextureOriginID,
                           GLuint depthTextureProcessedID,
                           std::vector<std::pair<int, float> > materialTextureAndDepth,
                           float xOffset,
                           float yOffset,
                           float depthScale) {

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

    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, depthTextureProcessedID);
    glUniform1i(depthImageTextureProcessedUniform, 1);

    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, depthTextureOriginID);
    glUniform1i(depthImageTextureOriginUniform, 2);

    for (int i = 0; i < materialTextureAndDepth.size(); i++) {
        glUniform1f(materialDepthUniform[i],
            materialTextureAndDepth[i].second * MATERIAL_DEPTH_MAX);
        if (materialTextureAndDepth[i].second >= 0.0f) {
            glActiveTexture(GL_TEXTURE3 + i);
            glBindTexture(GL_TEXTURE_2D, materialTextureAndDepth[i].first);
            glUniform1i(materialImageTextureUniform[i], 3 + i);
        }
    }

    //传入其他参数
    glUniform1f(focusUniform, 0.5f);
    glUniform1f(scaleUniform, depthScale);
    glUniform2f(xyOffsetUniform, xOffset, yOffset);

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