#version 330 core

in vec3 vertPosition;
in vec3 vertNormal;
in vec2 vertTextureCoordinates;

uniform vec3 viewPosition;

uniform vec3 lightPosition;
uniform vec3 lightColor;

uniform int hasTexture;
uniform vec3 objectColor;
uniform sampler2D objectTexture;

uniform sampler2D depthTexture;

out vec4 fragColor;

float near = 0.1f;
float far = 100.0f;

float linearizeDepth(float depth) {
	return (2.0 * near * far) / (far + near - (depth * 2.0 - 1.0) * (far - near));
}

void main() {
	
	// Ambient
	vec3 ambient = 0.8f * lightColor;
	
	// Diffuse
	vec3 normal = normalize(vertNormal);
	vec3 lightDirection = normalize(lightPosition - vertPosition);
	float diffuseStrength = max(dot(normal, lightDirection), 0.0);
	vec3 diffuse = lightColor * diffuseStrength;
	
	// Specular
	float specularStrength = 0.5;
	vec3 viewDirection = normalize(viewPosition - vertPosition);
	vec3 reflectDirection = reflect(-lightDirection, normal);
	float spec = pow(max(dot(viewDirection, reflectDirection), 0.0), 16);
	vec3 specular = specularStrength * spec * lightColor;
	
	vec3 result = (ambient + diffuse + specular);
	if (hasTexture == 1) {
		result *= texture(objectTexture, vertTextureCoordinates).xyz;
	} else {
		result *= objectColor;
	}
//	fragColor = vec4(result, 1.0);

//	vec2 normalizedUV = vec2(gl_FragCoord.x / 1280, gl_FragCoord.y / 720);
	ivec2 normalizedUV = ivec2(gl_FragCoord.xy);
	float currentDepth = texelFetch(depthTexture, normalizedUV, 0).r;
	float depth = 0.0005;
	for (int i = -1; i < 2; i++) {
		for (int j = -1; j < 2; j++) {
			float diff = currentDepth - texelFetchOffset(depthTexture, normalizedUV, 0, ivec2(i, j)).r;
			if (diff > depth) {
				depth = diff;
			}
		}
	}
	
	if (depth > 0.0005) {
		result = vec3(0.0);
	}
	fragColor = vec4(result, 1.0);

//	float depth = texture(depthTexture, normalizedUV).x; 
//	fragColor = vec4(vec3(texture(depthTexture, normalizedUV).x), 1.0);
	
}