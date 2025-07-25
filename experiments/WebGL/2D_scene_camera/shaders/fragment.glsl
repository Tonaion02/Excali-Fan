#version 300 es    
precision highp float;

in vec4 fin_color;

out vec4 fragColor;

void main()
{
    fragColor = fin_color;
}