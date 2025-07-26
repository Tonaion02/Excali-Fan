#version 300 es
in vec2 a_coordinates;
in vec2 a_texcoord;

uniform vec2 u_resolution;
uniform mat3 u_camera_transform;

out vec2 v_textcoord;

void main()
{
    vec2 position = (u_camera_transform * vec3(a_coordinates, 1.0)).xy;    

    vec2 zeroToOne = position / u_resolution;
    vec2 zeroToTwo = zeroToOne * 2.0;
    vec2 clipSpace = zeroToTwo - 1.0;
    
    gl_Position = vec4(clipSpace * vec2(1, -1), 0, 1);

    v_textcoord = a_texcoord;
}