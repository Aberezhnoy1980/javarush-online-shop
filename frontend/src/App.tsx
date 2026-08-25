import { useEffect, useState } from 'react'
import './App.css'

type HealthState = 'loading' | 'UP' | 'DOWN'

function useHealth(url: string): HealthState {
  const [status, setStatus] = useState<HealthState>('loading')

  useEffect(() => {
    let cancelled = false

    fetch(url)
      .then((response) => (response.ok ? response.json() : Promise.reject()))
      .then((body: { status?: string }) => {
        if (!cancelled) {
          setStatus(body.status === 'UP' ? 'UP' : 'DOWN')
        }
      })
      .catch(() => {
        if (!cancelled) {
          setStatus('DOWN')
        }
      })

    return () => {
      cancelled = true
    }
  }, [url])

  return status
}

function HealthBadge({ label, status }: { label: string; status: HealthState }) {
  return (
    <li>
      <span>{label}</span>
      <strong className={status === 'UP' ? 'ok' : status === 'DOWN' ? 'bad' : ''}>
        {status === 'loading' ? '…' : status}
      </strong>
    </li>
  )
}

function App() {
  const shopHealth = useHealth('/actuator/health')

  return (
    <main className="page">
      <h1>JavaRush Online Shop</h1>
      <p>
        Скелет приложения: каталог, корзина и оплата появятся в следующих
        инкрементах.
      </p>
      <ul className="health">
        <HealthBadge label="shop-service" status={shopHealth} />
      </ul>
      <p className="links">
        <a href="/actuator/health">shop health</a>
        <a href="http://localhost:8081/actuator/health">payment health</a>
      </p>
    </main>
  )
}

export default App
