import { describe, expect, it, vi } from 'vitest'
import type { IScannerControls } from '@zxing/browser'
import { createTicketScanner, type VideoQrReader } from '@/admission/api/ticketScanner.ts'

function controlsStub(): IScannerControls & { stop: ReturnType<typeof vi.fn> } {
  return { stop: vi.fn() } as unknown as IScannerControls & { stop: ReturnType<typeof vi.fn> }
}

function pendingReader(): {
  reader: VideoQrReader
  resolve: (controls: IScannerControls) => void
  callback: (result: { getText(): string } | undefined) => void
} {
  let resolve: (controls: IScannerControls) => void = () => {}
  let callback: (result: { getText(): string } | undefined) => void = () => {}
  const reader: VideoQrReader = {
    decodeFromVideoDevice: vi.fn(
      (_deviceId, _video, onResult) =>
        new Promise<IScannerControls>((resolvePromise) => {
          callback = onResult
          resolve = resolvePromise
        }),
    ),
  }
  return { reader, resolve: (controls) => resolve(controls), callback: (result) => callback(result) }
}

describe('createTicketScanner', () => {
  it('reports trimmed tokens and stops the live stream', async () => {
    const pending = pendingReader()
    const scanner = createTicketScanner(() => pending.reader)
    const onToken = vi.fn()

    const start = scanner.start(document.createElement('video'), onToken)
    const controls = controlsStub()
    pending.resolve(controls)
    await start

    pending.callback({ getText: () => '  qr-token  ' })
    expect(onToken).toHaveBeenCalledWith('qr-token')

    pending.callback(undefined)
    pending.callback({ getText: () => '   ' })
    expect(onToken).toHaveBeenCalledTimes(1)

    scanner.stop()
    expect(controls.stop).toHaveBeenCalledTimes(1)
  })

  it('stops a stream that finishes starting after stop was requested', async () => {
    const pending = pendingReader()
    const scanner = createTicketScanner(() => pending.reader)

    const start = scanner.start(document.createElement('video'), vi.fn())
    scanner.stop()
    const controls = controlsStub()
    pending.resolve(controls)
    await start

    expect(controls.stop).toHaveBeenCalledTimes(1)

    scanner.stop()
    expect(controls.stop).toHaveBeenCalledTimes(1)
  })

  it('invalidates a pending start superseded by a newer one', async () => {
    const first = pendingReader()
    const second = pendingReader()
    const readers = [first.reader, second.reader]
    const scanner = createTicketScanner(() => readers.shift() ?? second.reader)
    const video = document.createElement('video')

    const firstStart = scanner.start(video, vi.fn())
    const secondStart = scanner.start(video, vi.fn())

    const staleControls = controlsStub()
    first.resolve(staleControls)
    await firstStart
    expect(staleControls.stop).toHaveBeenCalledTimes(1)

    const liveControls = controlsStub()
    second.resolve(liveControls)
    await secondStart
    expect(liveControls.stop).not.toHaveBeenCalled()

    scanner.stop()
    expect(liveControls.stop).toHaveBeenCalledTimes(1)
  })
})
