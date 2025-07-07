#version 330 core

out vec4 fragColor;

float near = 0.1f;
float far = 100.0f;

float linearizeDepth(float depth) {
	return (2.0 * near * far) / (far + near - (depth * 2.0 - 1.0) * (far - near));
}

float logisticDepth(float depth, float steepness = 0.5f, float offset = 5.0f) {
	float zVal = linearizeDepth(depth);
	return (1 / (1 + exp(-steepness * (zVal - offset))));
}

void main() {
	
	// Normalized depth rendering
	fragColor = vec4(vec3(linearizeDepth(gl_FragCoord.z) / far), 1.0);

	// "Fog" depth rendering
//	float depth = logisticDepth(gl_FragCoord.z);
//	fragColor = vec4(result, 1.0) * (1.0f - depth) + vec4(depth * vec3(0.0, 0.0, 0.0), 1.0);
	
}