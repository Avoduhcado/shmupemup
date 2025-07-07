#version 330 core

in vec4 gColor;

out vec4 fragColor;

float circ(vec2 uv, vec2 pos, float radius, float blur) {
	float dist = length(uv-pos);
	return smoothstep(radius+blur, radius-blur, dist);
}

void main() {
	
//	float star = circ(gl_FragCoord.xy, vertPosition, 0.07, 0.001);
//	float star = smoothstep(0.0, 1.0, length(gl_FragCoord.xy - geoPosition));
	
//	star *= 1.0 - circ(gl_FragCoord.xy, geoPosition + vec2(0.03), 0.07, 0.001) * 0.95;
//	star = clamp(star, 0, 1);
	
//	vec4 color = vec4(gColor, star);
//	color.rgb *= 0.8;
	
	fragColor = gColor;
	
}