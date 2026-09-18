import { BrowserQRCodeReader, type IScannerControls } from '@zxing/browser'

/**
 * Watches a video element for Ticket QR codes and reports each decoded
 * Admission token. The camera stream stops through `stop`.
 */
export type TicketScanner = {
  start: (video: HTMLVideoElement, onToken: (token: string) => void) => Promise<void>
  stop: () => void
}

export function createTicketScanner(): TicketScanner {
  let controls: IScannerControls | null = null
  return {
    async start(video, onToken) {
      const reader = new BrowserQRCodeReader()
      controls = await reader.decodeFromVideoDevice(undefined, video, (result) => {
        const token = result?.getText().trim()
        if (token) {
          onToken(token)
        }
      })
    },
    stop() {
      controls?.stop()
      controls = null
    },
  }
}
