#version 300 es
precision highp float;
      
in vec2 a_position;
in vec4 a_color;
      
out vec4 fin_color;

uniform vec2 u_resolution;
uniform mat3 u_camera_transform;
// T: TODO: remove useless uniforms
uniform vec2 u_translation;
uniform vec2 u_rotation;

void main() 
{
    // T: TODO: remove these useless operations
    vec2 rotated_position = vec2(a_position.x * u_rotation.y + a_position.y * u_rotation.x, a_position.y * u_rotation.y - a_position.x * u_rotation.x);
    vec2 position_1 = rotated_position + u_translation;

    vec2 position = (u_camera_transform * vec3(a_position, 1.0)).xy;

    vec2 zeroToOne = position / u_resolution;
    vec2 zeroToTwo = zeroToOne * 2.0;
    vec2 clipSpace = zeroToTwo - 1.0;
    fin_color = a_color;
    gl_Position = vec4(clipSpace * vec2(1, -1), 0, 1);
}