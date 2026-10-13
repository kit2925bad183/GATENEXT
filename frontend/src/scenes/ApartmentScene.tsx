import React, { Suspense, useRef, useState, useEffect } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import * as THREE from 'three';
import { ApartmentSvgFallback } from './ApartmentSvgFallback';

interface ApartmentSceneProps {
  className?: string;
  autoRotate?: boolean;
}

// Low-poly 3D Apartment Model
const ApartmentModel: React.FC<{ autoRotate?: boolean }> = ({ autoRotate = true }) => {
  const groupRef = useRef<THREE.Group>(null);

  useFrame((_, delta) => {
    if (autoRotate && groupRef.current) {
      // Gentle subtle rotation
      groupRef.current.rotation.y += delta * 0.12;
    }
  });

  return (
    <group ref={groupRef} position={[0, -1.2, 0]}>
      {/* Ground plane */}
      <mesh receiveShadow position={[0, -0.05, 0]} rotation={[-Math.PI / 2, 0, 0]}>
        <planeGeometry args={[14, 14]} />
        <meshStandardMaterial color="#F7F9FC" roughness={0.8} />
      </mesh>

      {/* Outer circular pathway */}
      <mesh receiveShadow position={[0, -0.04, 0]} rotation={[-Math.PI / 2, 0, 0]}>
        <ringGeometry args={[3.2, 4.2, 32]} />
        <meshStandardMaterial color="#EDF3FF" roughness={0.6} />
      </mesh>

      {/* Main Center Tower (Block A) */}
      <mesh castShadow receiveShadow position={[0, 2.2, 0]}>
        <boxGeometry args={[1.8, 4.4, 1.8]} />
        <meshStandardMaterial color="#FFFFFF" roughness={0.2} metalness={0.05} />
      </mesh>
      {/* Tower Roof Accent */}
      <mesh position={[0, 4.45, 0]}>
        <boxGeometry args={[1.9, 0.15, 1.9]} />
        <meshStandardMaterial color="#2855D9" roughness={0.3} />
      </mesh>
      {/* Center Tower Windows & Balconies */}
      {[-1.4, -0.7, 0, 0.7, 1.4].map((y, idx) => (
        <group key={`center-floor-${idx}`} position={[0, y + 2.2, 0]}>
          {/* Front balcony */}
          <mesh castShadow position={[0, 0, 0.95]}>
            <boxGeometry args={[1.2, 0.35, 0.25]} />
            <meshStandardMaterial color="#087F8C" roughness={0.3} />
          </mesh>
          {/* Windows Left & Right */}
          <mesh position={[-0.45, 0.2, 0.91]}>
            <planeGeometry args={[0.3, 0.4]} />
            <meshStandardMaterial color="#EDF3FF" roughness={0.1} />
          </mesh>
          <mesh position={[0.45, 0.2, 0.91]}>
            <planeGeometry args={[0.3, 0.4]} />
            <meshStandardMaterial color="#EDF3FF" roughness={0.1} />
          </mesh>
        </group>
      ))}

      {/* Left Tower (Block B) */}
      <mesh castShadow receiveShadow position={[-2.4, 1.6, -0.5]}>
        <boxGeometry args={[1.5, 3.2, 1.5]} />
        <meshStandardMaterial color="#FFFFFF" roughness={0.2} />
      </mesh>
      <mesh position={[-2.4, 3.25, -0.5]}>
        <boxGeometry args={[1.6, 0.12, 1.6]} />
        <meshStandardMaterial color="#087F8C" roughness={0.3} />
      </mesh>
      {/* Left Tower Windows */}
      {[-0.9, -0.2, 0.5].map((y, idx) => (
        <mesh key={`b-win-${idx}`} position={[-2.4, y + 1.6, 0.26]}>
          <boxGeometry args={[1.0, 0.35, 0.05]} />
          <meshStandardMaterial color="#EDF3FF" roughness={0.1} />
        </mesh>
      ))}

      {/* Right Tower (Block C) */}
      <mesh castShadow receiveShadow position={[2.4, 1.8, -0.3]}>
        <boxGeometry args={[1.6, 3.6, 1.5]} />
        <meshStandardMaterial color="#FFFFFF" roughness={0.2} />
      </mesh>
      <mesh position={[2.4, 3.65, -0.3]}>
        <boxGeometry args={[1.7, 0.12, 1.7]} />
        <meshStandardMaterial color="#2855D9" roughness={0.3} />
      </mesh>
      {/* Right Tower Windows */}
      {[-1.1, -0.4, 0.3, 1.0].map((y, idx) => (
        <mesh key={`c-win-${idx}`} position={[2.4, y + 1.8, 0.46]}>
          <boxGeometry args={[1.1, 0.35, 0.05]} />
          <meshStandardMaterial color="#EDF3FF" roughness={0.1} />
        </mesh>
      ))}

      {/* Front Entrance Security Gatehouse */}
      <group position={[0, 0.3, 2.8]}>
        {/* Gatehouse Building */}
        <mesh castShadow position={[0, 0, 0]}>
          <boxGeometry args={[0.9, 0.6, 0.7]} />
          <meshStandardMaterial color="#FFFFFF" roughness={0.3} />
        </mesh>
        <mesh position={[0, 0.35, 0]}>
          <boxGeometry args={[1.0, 0.08, 0.8]} />
          <meshStandardMaterial color="#2855D9" roughness={0.3} />
        </mesh>
        {/* Guard Booth Glass */}
        <mesh position={[0, 0.05, 0.36]}>
          <planeGeometry args={[0.6, 0.3]} />
          <meshStandardMaterial color="#EDF3FF" transparent opacity={0.8} />
        </mesh>
        {/* Security Access Boom Barrier */}
        <mesh position={[-0.8, 0.2, 0]} rotation={[0, 0, 0]}>
          <boxGeometry args={[0.9, 0.06, 0.06]} />
          <meshStandardMaterial color="#2855D9" />
        </mesh>
        <mesh position={[0.8, 0.2, 0]} rotation={[0, 0, 0]}>
          <boxGeometry args={[0.9, 0.06, 0.06]} />
          <meshStandardMaterial color="#087F8C" />
        </mesh>
      </group>

      {/* Small Low-Poly Trees */}
      {[
        [-1.3, 1.8],
        [1.3, 1.8],
        [-3.2, 1.2],
        [3.3, 1.0],
        [-1.5, -2.0],
        [1.6, -2.1],
      ].map(([x, z], idx) => (
        <group key={`tree-${idx}`} position={[x, 0, z]}>
          {/* Trunk */}
          <mesh castShadow position={[0, 0.25, 0]}>
            <cylinderGeometry args={[0.06, 0.08, 0.5, 6]} />
            <meshStandardMaterial color="#58677D" roughness={0.9} />
          </mesh>
          {/* Foliage Cone 1 */}
          <mesh castShadow position={[0, 0.6, 0]}>
            <coneGeometry args={[0.35, 0.6, 6]} />
            <meshStandardMaterial color="#087F8C" roughness={0.7} />
          </mesh>
          {/* Foliage Cone 2 */}
          <mesh castShadow position={[0, 0.9, 0]}>
            <coneGeometry args={[0.25, 0.5, 6]} />
            <meshStandardMaterial color="#137547" roughness={0.7} />
          </mesh>
        </group>
      ))}
    </group>
  );
};

export const ApartmentScene: React.FC<ApartmentSceneProps> = ({
  className = '',
  autoRotate = true,
}) => {
  const [hasWebGL, setHasWebGL] = useState<boolean>(true);
  const [prefersReducedMotion, setPrefersReducedMotion] = useState<boolean>(false);

  useEffect(() => {
    // Check WebGL availability
    try {
      const canvas = document.createElement('canvas');
      const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
      if (!gl) {
        setHasWebGL(false);
      }
    } catch (e) {
      setHasWebGL(false);
    }

    // Check reduced motion query
    const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
    setPrefersReducedMotion(mediaQuery.matches);
    const handler = (e: MediaQueryListEvent) => setPrefersReducedMotion(e.matches);
    mediaQuery.addEventListener('change', handler);
    return () => mediaQuery.removeEventListener('change', handler);
  }, []);

  if (!hasWebGL) {
    return <ApartmentSvgFallback className={className} />;
  }

  return (
    <div className={`w-full h-full relative select-none overflow-hidden bg-surface ${className}`}>
      <Canvas
        shadows
        camera={{ position: [5, 4, 7], fov: 38 }}
        dpr={Math.min(1.5, window.devicePixelRatio || 1)}
        gl={{ antialias: true, alpha: true }}
        style={{ pointerEvents: 'none' }}
      >
        <ambientLight intensity={0.9} />
        <directionalLight
          position={[6, 10, 5]}
          intensity={1.2}
          castShadow
          shadow-mapSize-width={1024}
          shadow-mapSize-height={1024}
          shadow-camera-near={0.5}
          shadow-camera-far={25}
          shadow-camera-left={-6}
          shadow-camera-right={6}
          shadow-camera-top={6}
          shadow-camera-bottom={-6}
          shadow-bias={-0.0005}
        />
        <directionalLight position={[-4, 6, -4]} intensity={0.4} color="#EDF3FF" />

        <Suspense fallback={null}>
          <ApartmentModel autoRotate={autoRotate && !prefersReducedMotion} />
        </Suspense>
      </Canvas>

      {/* Clean Status Overlay */}
      <div className="absolute bottom-4 left-4 right-4 flex items-center justify-between text-xs text-text-secondary bg-white/90 backdrop-blur-sm px-3.5 py-2 rounded-lg border border-border">
        <div className="flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-[#137547] animate-pulse" />
          <span className="font-medium text-text-main">GateNest 3D Complex Visualizer</span>
        </div>
        <span className="text-[11px] text-text-secondary">Low-Poly Architectural Preview</span>
      </div>
    </div>
  );
};
