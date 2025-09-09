uniform sampler2D u_texture; // == src
uniform sampler2D u_dst;

varying vec2 v_texCoords;

void main()
{
    // src illuminates dst
    vec4 src = texture2D(u_texture, v_texCoords);
    vec4 dst = texture2D(u_dst    , v_texCoords);

    gl_FragColor.a = dst.a;
    gl_FragColor.rgb = dst.rgb + src.rgb * min(dst.rgb, vec3(1) - dst.rgb);
}
