import { BrowserQRCodeReader, type IScannerControls } from '@zxing/browser'

/**
 * Watches a video element for Ticket QR codes and reports each decoded
 * Admission token. The camera stream stops through `stop`.
 */
export type TicketScanner = {
  start: (video: HTMLVideoElement, onToken: (token: string) => void) => Promise<void>
  stop: () => void
}

export type VideoQrReader = {
  decodeFromVideoDevice: (
    deviceId: string | undefined,
    video: HTMLVideoElement,
    callback: (result: { getText(): string } | undefined) => void,
  ) => Promise<IScannerControls>
}

export function createTicketScanner(
  createReader: () => VideoQrReader = () => new BrowserQRCodeReader(),
): TicketScanner {
  let controls: IScannerControls | null = null
  // Bumped by stop and by every new start, so a camera stream that finishes
  // starting after it was superseded or stopped shuts itself down instead of
  // leaking a live stream nobody can reach.
  let generation = 0

  function stop() {
    generation += 1
    controls?.stop()
    controls = null
  }

  return {
    async start(video, onToken) {
      stop()
      const expected = generation
      const session = await createReader().decodeFromVideoDevice(undefined, video, (result) => {
        const token = result?.getText().trim()
        if (token) {
          onToken(token)
        }
      })
      if (expected !== generation) {
        session.stop()
        return
      }
      controls = session
    },
    stop,
  }
}
