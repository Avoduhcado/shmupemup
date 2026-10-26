#version 330 core

out vec4 fragColor;

float near = 0.1f;
float far = 100.0f;

float linearizeDepth(float depth) {
	float z = depth * 2.0 - 1.0;

	return (2.0 * near * far) / (far + near - z * (far - near));
}

float logisticDepth(float depth, float steepness = 0.5f, float offset = 5.0f) {
	float zVal = linearizeDepth(depth);
	return (1 / (1 + exp(-steepness * (zVal - offset))));
}

void main() {
	float linearDepth = linearizeDepth(gl_FragCoord.z) / far;
	
	// Normalized depth rendering
	fragColor = vec4(vec3(linearDepth), 1.0);

	// "Fog" depth rendering
//	float depth = logisticDepth(gl_FragCoord.z);
//	fragColor = vec4(result, 1.0) * (1.0f - depth) + vec4(depth * vec3(0.0, 0.0, 0.0), 1.0);
	
}