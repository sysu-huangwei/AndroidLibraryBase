//
// Created by rayyy on 2020/8/5.
//

#ifndef SMARTKIT_BASEGLUTILS_H
#define SMARTKIT_BASEGLUTILS_H

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

namespace threedimensional {

/**
 * Description: OpenGLES2.0 常用方法
 */
class BaseGLUtils {
 public:

  /**
   * 根据指定的着色器类型和脚本生成着色器。
   *
   * @param shaderType 着色器类型
   * @param source 着色器脚本
   * @return 着色器
   */
  static GLuint loadShader(int shaderType, const char *source);

  /**
   * 根据指定的着色器脚本创建着色器程序。
   *
   * @param vertexSource 顶点着色器脚本
   * @param fragmentSource 片段着色器脚本
   * @return 着色器程序
   */
  static GLuint createProgram(const char *vertexSource, const char *fragmentSource);

  /**
   * 根据指定的参数创建纹理。
   *
   * @param target 纹理类型
   * @param wrapMode 纹理环绕方式
   * @param filterMode 纹理过滤方式
   * @return 要创建的纹理
   */
  static GLuint createTexture(int target, int wrapMode, int filterMode);

  /**
   * 创建 2D 纹理
   * @return 要创建的纹理
   */
  static GLuint createTexture2D();

  /**
   * 创建 FBO
   * @param texture 要绑定这个fbo的纹理
   * @return 要创建的FBO
   */
  static GLuint createFBO(int texture, int width, int height);

};

}

#endif //SMARTKIT_BASEGLUTILS_H
