export function round5(value?: number): number {
  return value ? Math.round(value * 100000) / 100000 : 0;
}
